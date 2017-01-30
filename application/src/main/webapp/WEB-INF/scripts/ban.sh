#!/bin/sh
# RestComm-SBC Banning script
# Oscar Carriles <ocarriles@eolos.la>
# CentOS 6 iptables
#------------------------------------
FIREWALL="/sbin/iptables"
FIREWALL_COMMIT="/sbin/iptables-save"
FIREWALL_RESTART="service iptables restart"
TEE="/usr/bin/tee"
CONFIG="/etc/sysconfig/iptables"
ACTION=$1
IP=$2

[ -f $CONFIG ] ||  exit -1;

commit() {
$FIREWALL_COMMIT | $TEE $CONFIG
}
restart() {
$FIREWALL_RESTART
}

case "$ACTION" in
add)
        [ "X$IP" !=  "X" ] || exit -1;
        $FIREWALL -A INPUT -s $IP -j DROP
        ;;
remove)
        ;;
allow)
        [ "X$IP" != "X" ] || exit -1;
        $FIREWALL -A INPUT -s $IP -j ACCEPT
        ;;
flush)
        $FIREWALL -F
        ;;
commit)
        commit
        restart
        ;;
*)
        echo "Usage: $0 [add|remove|allow] IP_ADDRESS"
        echo "$0 [flush|commit]"
        exit -1;
esac
