#!/bin/bash
mainPath="/opt/enum4linux-ng"
. $mainPath/.config

start=$SECONDS

for i in "${ranges[@]}"
do
	while read ipRange; do
		echo "Nmap scan - ${ipRange}"
		parallel -j 8 nmap -oN $mainPath/$i-list --excludefile $mainPath/ranges/ex -sP -T4 $ipRange \
		&& cat $mainPath/$i-list | awk '/Nmap scan report/{print $NF}' | sed 's/(//g' | sed 's/)//g' >> "$mainPath/ip-list-nmap-$i" \
		&&  cat $mainPath/$i-list >> $mainPath/ip-list-$i
	done <"$inPath/$i"

	echo "enum4linux-ng scan - ip-list-nmap-$i"

	while read ip; do

		python3 $mainPath/enum4linux-ng.py $ip -A -C -w NIX -u ${ldap_name} -p ${ldap_pwd} -k "administrator,guest,krbtgt,domain admins,root,bin,none,lxuser,superuser" -oJ $mainPath/$ip-result
		varIP=`cat $mainPath/$ip-result.json | jq -r '"IP: \(.target|.host) - "'`
		cat $mainPath/$ip-result.json | jq -r '.shares | keys[] as $k | "\($k), \(.[$k] | del(.type) | del(.comment) | select( .access.mapping == "ok" and .access.listing == "ok" ) | del(.access))"' \
		| tr -d ', {}' | grep -Ewv 'IPC\$|NETLOGON|SYSVOL|prnproc\$|print\$' | sed '$!s/$/, /' | xargs echo -n > $mainPath/$ip-result.txt
		varShares=`cat $mainPath/$ip-result.txt`
		varShares="$varIP $varShares"

		if [ "$varShares" != "$varIP " ];
		then
			varIP=`cat $mainPath/$ip-result.json | jq -r '"\(.target|.host)"'`
			varHost=`cat $mainPath/ip-list-$i | grep $varIP | awk '/Nmap scan report/{print $5}'`
			if [ "$varHost" == "$varIP" ];
                	then
	                        varIP=`cat $mainPath/$ip-result.json | jq -r '"OS: \(.os_info|.OS)"' | cut -f1 -d","`
                        	varShares="$varShares ($varIP)"
			else
		                varIP=`cat $mainPath/$ip-result.json | jq -r '"IP: \(.target|.host) "'`
				varShares=`cat $mainPath/$ip-result.txt`
                                varShares="$varIP ($varHost) - $varShares"
                                varIP=`cat $mainPath/$ip-result.json | jq -r '"OS: \(.os_info|.OS)"' | cut -f1 -d","`
		                varShares="$varShares ($varIP)"
			fi
			echo -e $varShares >> $outPath/results-scan.txt
		fi

		rm -rf $mainPath/$ip-result.json $mainPath/$ip-result.txt

	done <"$mainPath/ip-list-nmap-$i"
	rm -rf $mainPath/ip-list-$i $mainPath/ip-list-nmap-$i $mainPath/$i-list
done

duration=$(( SECONDS - start ))
let "hours=SECONDS/3600"
let "minutes=($duration%3600)/60"
let "seconds=(SECONDS%3600)%60"
timeFinal="Completed in $hours hour(s), $minutes minute(s) and $seconds second(s)"
echo -e "Report about shared resources: `date '+%d-%m-%Y--%H:%M:%S'`\n\n$timeFinal\n\n" > $outPath/results.txt
cat $outPath/results-scan.txt >> $outPath/results.txt

swaks -f ${mailFrom} -t ${mailTo} -s smtp.nixsolutions.com --auth-user=${mailAuth} --auth-password=${mailPass} -tlsc -p 465 --body $outPath/results.txt --header "Subject: Report about shared resources (enum4linux-ng)" --add-header "Content-Type: text/plain; charset=UTF-8"
rm -rf ${outPath}/results.txt ${outPath}/results-scan.txt
