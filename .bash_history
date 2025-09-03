help
gcloud config set project sgs-playvox
sudo find / -name version.sh
sudo systemctl status tomcat
ps -ef | grep tomcat
which tomcat
ls /opt/tomcat/
tail -f /opt/tomcat/logs/catalina.out
gcloud run services list --platform managed
clear
gcloud run services describe actionlog-prod   --platform managed   --region us-central1   --format="value(spec.template.spec.containers[0].image)"
gcloud run services describe actionlog-prod   --platform managed   --region us-central1   --format="value(spec.template.spec.containers[0].gcr.io/sgs-playvox/github.com/khorospv/actionlog@sha256:5a29f96110f5fe94bdd2a6d1a910ec9fd29415f90f2a5cca216b802612210354)"
gcloud artifacts docker images describe gcr.io/sgs-playvox/github.com/khorospv/actionlog@sha256:5a29f96110f5fe94bdd2a6d1a910ec9fd29415f90f2a5cca216b802612210354   --show-package-vulnerabilities
gcloud artifacts docker images describe gcr.io/sgs-playvox/github.com/khorospv/actionlog@sha256:5a29f96110f5fe94bdd2a6d1a910ec9fd29415f90f2a5cca216b802612210354   --show-package-vulnerabilities   --format=yaml
clear
gcloud artifacts docker images describe gcr.io/sgs-playvox/github.com/khorospv/actionlog@sha256:5a29f96110f5fe94bdd2a6d1a910ec9fd29415f90f2a5cca216b802612210354     --show-package-vulnerability
clear
gcloud run services exec actionlog-prod   --platform managed   --region us-central1   -- /bin/sh
version
--version
--v
clear
gcloud help -- SEARCH_TERMS
LEAR
cler
cancle
clear
grep -i "tomcat" Dockerfile
gcloud builds list --filter="tags=actionlog-prod" --limit=1
gcloud builds describe BUILD_ID | grep -i "tomcat"
claer
gcloud builds list --filter="tags=actionlog-prod" --limit=1
clear
gcloud builds describe BUILD_ID | grep -i "tomcat"
IMAGE_URL=$(gcloud run services describe actionlog-prod \
  --region us-central1 \
  --format="value(spec.template.spec.containers[0].image)")
IMAGE_URL=$(gcloud run services describe actionlog-prod \
  --region us-central1 \
  --format="value(spec.template.spec.containers[0].image)")
# Check vulnerabilities (including Tomcat version)
gcloud artifacts docker images describe $IMAGE_URL   --show-package-vulnerability   --format=json | grep -i "tomcat"
clear
$CATALINA_HOME/bin/version.sh
clear
find / -name "version.sh" 2>/dev/null
/path/to/tomcat/bin/version.sh
unzip -p /path/to/tomcat/lib/catalina.jar META-INF/MANIFEST.MF | grep "Specification-Version"
dpkg -l | grep tomcat
clear
startup.sh
clear
gcloud artifacts docker images describe gcr.io/sgs-playvox/github.com/khorospv/actionlog@sha256:5a29f96110f5fe94bdd2a6d1a910ec9fd29415f90f2a5cca216b802612210354 --show-package-vulnerability
clear
cd /path/to/your/project
ls -l Dockerfile  # Check if it exists
clear
TOMCAT_VERSION=10.1.18  # Change this to the latest version you want
cd /tmp
curl -O https://downloads.apache.org/tomcat/tomcat-10/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz
curl http://localhost:8080
python3 -m http.server 8080
help
sudo tar xzvf apache-tomcat-$TOMCAT_VERSION.tar.gz -C /opt/
sudo mv /opt/apache-tomcat-$TOMCAT_VERSION /opt/tomcat-$TOMCAT_VERSION
ls /opt/
sudo find / -type d -name "tomcat*" 2>/dev/null
ls /usr/share/
ls /usr/local/
ls /opt/tomcat
ls /usr/share/tomcat
sudo find / -type d -name "tomcat*" 2>/dev/null
sudo find / -type d -name "java*" 2>/dev/null
sudo find / -type d -name "python*" 2>/dev/null
sudo find / -type d -name "tomcat*" 2>/dev/null
java -version
gcloud config list
docker pull     gcr.io/sgs-playvox/github.com/khorospv/actionlog:5352db47a053db1f366fce41b8c9af99290d017e
ls
sudo docker images
sudo docker images inspect  d5bc13a8fd9
sudo docker  inspect  d5bc13a8fd9
sudo docker ps
gcloud source repos clone actionlog
ls
gcloud source repos clone author
gcloud source repos clone batchprocessing
gcloud source repos clone batchProcessing
gcloud source repos clone email
gcloud source repos clone monitor
gcloud source repos clone ployvoxBatch
gcloud source repos clone playvoxBatch
gcloud source repos clone tableDeletion
gcloud projects list
gcloud config get-value project
export PROD_PROJECT="sgs-playvox"
gcloud config set project "$PROD_PROJECT"
gcloud services list --enabled --format=json > prod.enabled-services.json
ls
cat prod.enabled-services.json 
gcloud projects get-iam-policy "$PROD_PROJECT" --format=json > prod.iam.json
ls
gcloud compute networks list --format=json > vpcs.json
ls
gcloud compute networks subnets list --regions=all --format=json > subnets.json
gcloud compute networks subnets list --format=json > subnets.json
ls
gcloud compute firewall-rules list --format=json > firewalls.json
ls
gcloud compute forwarding-rules list --format=json > fwd_rules.json
gcloud compute target-http-proxies list --format=json > target_http_proxies.json
gcloud compute target-https-proxies list --format=json > target_https_proxies.json
gcloud compute url-maps list --format=json > url_maps.json
gcloud compute backend-services list --global --format=json > backend_services_global.json
gcloud compute backend-services list --regions=all --format=json > backend_services_regional.json
gcloud compute health-checks list --format=json > health_checks.json
gcloud compute ssl-certificates list --format=json > ssl_certs.json
gcloud compute forwarding-rules list --format=json > fwd_rules.json
gcloud compute target-http-proxies list --format=json > target_http_proxies.json
gcloud compute target-https-proxies list --format=json > target_https_proxies.json
gcloud compute url-maps list --format=json > url_maps.json
gcloud compute backend-services list --global --format=json > backend_services_global.json
gcloud compute backend-services list --format=json > backend_services_regional.json
gcloud compute health-checks list --format=json > health_checks.json
gcloud compute ssl-certificates list --format=json > ssl_certs.json
ls
REGION="us-central1"
gcloud run services list --platform=managed --region="$REGION" --format=json > run_services.$REGION.json
# Optional: describe each service for env vars, VPC connectors, etc.
for SVC in $(gcloud run services list --platform=managed --region="$REGION" --format="value(metadata.name)"); do   gcloud run services describe "$SVC" --region="$REGION" --format=json > "run.$SVC.$REGION.json"; done
ls
gcloud sql instances list --format=json > sql_instances.json
for DB in $(gcloud sql instances list --format="value(name)"); do   gcloud sql instances describe "$DB" --format=json > "sql.$DB.json"; done
ls
gcloud pubsub topics list --format=json > pubsub_topics.json
gcloud pubsub subscriptions list --format=json > pubsub_subs.json
ls
gsutil ls -p "$PROD_PROJECT" > buckets.txt
# Per bucket metadata (loop over buckets)
while read B; do gsutil ls -Lb "$B" > "$(echo $B | tr '/:' '__').meta.txt"; done < buckets.txt
ls
gcloud scheduler jobs list --format=json > scheduler.json
for REGION in $(gcloud scheduler locations list --format="value(locationId)"); do   echo "Listing jobs in $REGION...";   gcloud scheduler jobs list --location=$REGION --format=json > scheduler.$REGION.json; done
gcloud scheduler jobs list   --location=us-central1   --format=json > scheduler.json
gcloud secrets list --format=json > secrets.json
# For each secret: policy (helps track which services use it)
for S in $(gcloud secrets list --format="value(name)"); do   gcloud secrets get-iam-policy "$S" --format=json > "secret.$S.iam.json"; done
gcloud artifacts repositories list --format=json > ar_repos.json
gcloud dns managed-zones list --format=json > dns_zones.json
for Z in $(gcloud dns managed-zones list --format="value(name)"); do   gcloud dns record-sets export "dns.$Z.yaml" --zone="$Z"; done
# Logging sinks
gcloud logging sinks list --format=json > logging_sinks.json
# Monitoring alert policies & uptime checks
gcloud monitoring policies list --format=json > monitoring_policies.json
gcloud monitoring channels list --format=json > monitoring_channels.json
gcloud monitoring uptime-checks list --format=json > uptime_checks.json
gcloud logging sinks list --format=json > logging_sinks.json
gcloud monitoring policies list --format=json > monitoring_policies.json
gcloud monitoring channels list --format=json > monitoring_channels.json
gcloud monitoring uptime-checks list --format=json > uptime_checks.json
gcloud alpha monitoring policies list   --format=json > monitoring_policies.json
gcloud beta monitoring policies list   --format=json > monitoring_policies.json
gcloud alpha monitoring channels list   --format=json > monitoring_channels.json
gcloud monitoring uptime list-configs   --format=json > uptime_checks.json
EXPORT_DIR="$HOME/prod-export"
mkdir -p "$EXPORT_DIR"
gcloud beta resource-config bulk-export   --project="$PROD_PROJECT"   --resource-types=compute.googleapis.com/*,run.googleapis.com/*,sqladmin.googleapis.com/*,pubsub.googleapis.com/*,storage.googleapis.com/*   --output-path="$EXPORT_DIR"   --format=terraform
EXPORT_DIR="$HOME/prod-export"
gcloud beta resource-config bulk-export   --project="$PROD_PROJECT"   --resource-types=compute.googleapis.com/*,run.googleapis.com/*,sqladmin.googleapis.com/*,pubsub.googleapis.com/*,storage.googleapis.com/*   --path="$EXPORT_DIR"   --format=terraform
EXPORT_DIR="$HOME/prod-export"
gcloud beta resource-config bulk-export   --project="$PROD_PROJECT"   --path="$EXPORT_DIR"   --format=terraform
sudo apt-get install google-cloud-cli-config-connector
EXPORT_DIR="$HOME/prod-export"
gcloud beta resource-config bulk-export   --project="$PROD_PROJECT"   --path="$EXPORT_DIR"   --format=terraform
