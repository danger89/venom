#!/bin/bash
mainPath="/opt/smbmap"
. $mainPath/.config

start=$SECONDS

for i in "${ranges[@]}"
do

while read ipRange; do
    echo "Nmap scan - ${ipRange}"
    parallel -j 8 nmap -oN $mainPath/ip-list-$i --excludefile $mainPath/ranges/ex -sP -T4 $ipRange
    cat $mainPath/ip-list-$i | awk '/Nmap scan report/{print $NF}' | sed 's/(//g' | sed 's/)//g' >> "$mainPath/ip-list-nmap-$i"
done <"$inPath/$i"

echo "SMBMAP scan - ip-list-nmap-$i"
python3 $mainPath/smbmap.py --host-file $mainPath/ip-list-nmap-$i -u ${ldap_name} -p ${ldap_pwd} -d NIX -q --no-banner --exclude IPC$ print$ NETLOGON SYSVOL prnproc$ | grep -v -w "Working" | grep -v -w "Status: Guest session" >> "$outPath/results-$i.txt"

rm -rf $mainPath/ip-list-$i $mainPath/ip-list-nmap-$i
done

duration=$(( SECONDS - start ))
let "hours=SECONDS/3600"
let "minutes=($duration%3600)/60"
let "seconds=(SECONDS%3600)%60"
timeFinal="Completed in $hours hour(s), $minutes minute(s) and $seconds second(s)"
echo -e "Report about shared resources: `date '+%d-%m-%Y--%H:%M:%S'`\n\n$timeFinal\n\n" > $outPath/Report.txt
cat $outPath/*.txt >> $outPath/Report.txt

swaks -f ${mailFrom} -t ${mailTo} -s smtp.nixsolutions.com --auth-user=${mailAuth} --auth-password=${mailPass} -tlsc -p 465 --body $outPath/Report.txt --header "Subject: Report about shared resources (smbmap)" --add-header "Content-Type: text/plain; charset=UTF-8"
rm -rf ${outPath}/Report.txt $outPath/results-*.txt
