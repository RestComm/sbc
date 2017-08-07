#!/bin/sh
cd ${SBC_APP_DIR}/bin
. ./certs-docker.sh
OPTION=$1
CERTDIR="../conf/certs"
KEYSTORE="$CERTDIR/keystore"
KEYSTORE_WEB="$KEYSTORE-web.jks"
KEYSTORE_DTLS="$KEYSTORE-dtls.jks"



build() {
echo '<keytool> Creating SBC Web Server key ...'
keytool -genkey -noprompt \
 -keyalg RSA \
 -alias sbc-server-web \
 -keystore $KEYSTORE_WEB \
 -keypass changeME \
 -storepass changeME \
 -validity 365 \
 -keysize 2048 \
 -dname "CN=SBC Project, OU=Osky, O=sbc.eolos.la, C=US"

echo '<keytool> Creating SBC DTLS Server key ...'
keytool -genkey -noprompt \
 -keyalg EC \
 -alias sbc-server-dtls \
 -keystore $KEYSTORE_DTLS \
 -storepass changeME \
 -keypass changeME \
 -validity 365 \
 -keysize 256 \
 -sigalg SHA256withECDSA \
 -dname "CN=SBC Project, OU=Osky, O=sbc.eolos.la, C=US"

echo '<keytool> Exporting SBC DTLS Server Certificate ...'
keytool -exportcert -noprompt \
  -alias sbc-server-dtls  \
 -keystore $KEYSTORE_DTLS \
 -storepass changeME \
 -rfc \
 -file $CERTDIR/x509-server-ecdsa.pem
echo '<keytool> Importing SBC DTLS Keystore in portable format ...'
keytool -importkeystore -noprompt \
 -srcalias sbc-server-dtls \
 -srckeystore $KEYSTORE_DTLS \
 -destkeystore $CERTDIR/server.p12 \
 -srckeypass changeME \
 -srcstorepass changeME \
 -srcstoretype jks \
 -destkeypass changeME \
 -deststorepass changeME \
 -deststoretype pkcs12
echo '<openssl> Exporting SBC DTLS Private key ...'
openssl pkcs12 \
 -in $CERTDIR/server.p12 \
 -nocerts \
 -nodes \
 -password pass:changeME \
 -passin pass:changeME \
 -passout pass:changeME \
 -out $CERTDIR/x509-server-key-ecdsa.pem

}

rebuild() {
rm -rf $CERTDIR
mkdir $CERTDIR
build
}

patchStack() {
DIR=$PWD/..
sed "s#__PWD__#$DIR#g" ../conf/mss-sip-stack.properties.template > ../conf/mss-sip-stack.properties
}

buildcerts() {
patchStack
if [ ! -d $CERTDIR ]; then
        mkdir $CERTDIR
        build
else
	rebuild
fi
}

usage() {
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
config() {
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
#[ ! -z "$RPOLICY" ] && [ ! -z "$DOMAIN" ] && (config $DOMAIN $RPOLICY)
#./catalina.sh start

