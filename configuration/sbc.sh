#!/bin/sh
. ./certs.sh
OPTION=$1

function usage() {
	echo "Usage 1: $0 [-c] [-d <domain>] [-r <routing_policy>] -a start" 1>&2
	echo "Usage 2: $0 -a stop" 1>&2
	echo "i.e    : $0 -d cloud.eolos.la -r udp:192.168.1.2:5060 -a start"
	echo "options:"
	echo "a, action start|stop"
	echo "c, create certificates"
	echo "d, sip domain"
	echo "r, routing policy to back sip server"
	exit 1
}
function config() {
	echo "Applying configuration templates $1 $2 ..."
        sed "s#__DOMAIN__#$1#g; s#__RPOLICY__#$2#g" ../conf/sbc.xml.template > ../webapps/restcomm-sbc/WEB-INF/conf/sbc.xml
}


[ ! -f ../conf/certs/keystore-web.jks ] && buildcerts
[ ! -f ../webapps/restcomm-sbc/WEB-INF/conf/sbc.xml ]  && CONFIGURED=1

[ $# = 0 ] && usage

while getopts ":cd:r:a:" o; do
    case "${o}" in
	a)
	    case ${OPTARG} in
		start)
			ACTION="start"
			;;
		stop)
			ACTION="stop -force"
			;;
		*)
			usage
			;;
	    esac
	    ;;
	c)
	    buildcerts
	    ;;
        d)
            DOMAIN=${OPTARG}
            ;;
        r)
            RPOLICY=${OPTARG}
            ;;
        *)
            usage
            ;;
    esac 
done
[ ! -z "$RPOLICY" ] && [ ! -z "$DOMAIN" ] && (config $DOMAIN $RPOLICY)
./catalina.sh $ACTION

