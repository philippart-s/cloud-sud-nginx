# cloud-sud-nginx
Sources de l'opérateur Nginx pour Cloud Sud 2022

# Déroulé de la démo
## Initialisation du projet
 - [installer / mettre](https://sdk.operatorframework.io/docs/installation/) à jour la dernière version du [Operator SDK](https://sdk.operatorframework.io/) (v1.18.0 au moment de l'écriture du readme)
 - scaffolding du projet avec Quarkus : `operator-sdk init --plugins quarkus --domain fr.wilda --project-name cloud-sud-nginx`
 - l'arborescence générée est la suivante:
    ```bash
    .
    ├── LICENSE
    ├── Makefile
    ├── PROJECT
    ├── README.md
    ├── pom.xml
    ├── src
    │   └── main
    │       ├── java
    │       └── resources
    │           └── application.properties
    ```
 - vérification que cela compile : `mvn clean compile`
 - tester le lancement: `mvn quarkus:dev`
 - si l'erreur suivante survient:
    ```bash
    Exception in thread "main" java.lang.RuntimeException: java.lang.RuntimeException: java.lang.IllegalStateException: Hot deployment of the application is not supported when updating the Quarkus version. The application needs to be stopped and dev mode started up again
        at io.quarkus.deployment.dev.DevModeMain.start(DevModeMain.java:138)
        at io.quarkus.deployment.dev.DevModeMain.main(DevModeMain.java:62)
    ```
    Changer la version de la dépendance `quarkus.version` dans le `pom.xml` à `2.6.3.Final` voir bug [#74](https://github.com/operator-framework/java-operator-plugins/issues/74)
 - une fos corrigée Quarkus se lance, avec encore une erreur mais fois "normale" (il n'y a rien de définit):
    ```bash
    __  ____  __  _____   ___  __ ____  ______ 
    --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
    -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
    --\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
    2022-03-08 12:22:24,169 WARN  [io.fab.kub.cli.Config] (Quarkus Main Thread) Found multiple Kubernetes config files [[/Users/sphilipp/dev/ovh/k8s/kubeconfig.yml, /Users/sphilipp/dev/ovh/k8s/k8s-colima.yml, /Users/sphilipp/dev/ovh/k8s/kubeconfig-trillio.yml]], using the first one: [/Users/sphilipp/dev/ovh/k8s/kubeconfig.yml]. If not desired file, please change it by doing `export KUBECONFIG=/path/to/kubeconfig` on Unix systems or `$Env:KUBECONFIG=/path/to/kubeconfig` on Windows.

    2022-03-08 12:22:24,230 WARN  [io.fab.kub.cli.Config] (Quarkus Main Thread) Found multiple Kubernetes config files [[/Users/sphilipp/dev/ovh/k8s/kubeconfig.yml, /Users/sphilipp/dev/ovh/k8s/k8s-colima.yml, /Users/sphilipp/dev/ovh/k8s/kubeconfig-trillio.yml]], using the first one: [/Users/sphilipp/dev/ovh/k8s/kubeconfig.yml]. If not desired file, please change it by doing `export KUBECONFIG=/path/to/kubeconfig` on Unix systems or `$Env:KUBECONFIG=/path/to/kubeconfig` on Windows.
    2022-03-08 12:22:24,235 WARN  [io.fab.kub.cli.Config] (Quarkus Main Thread) Found multiple Kubernetes config files [[/Users/sphilipp/dev/ovh/k8s/kubeconfig.yml, /Users/sphilipp/dev/ovh/k8s/k8s-colima.yml, /Users/sphilipp/dev/ovh/k8s/kubeconfig-trillio.yml]], using the first one: [/Users/sphilipp/dev/ovh/k8s/kubeconfig.yml]. If not desired file, please change it by doing `export KUBECONFIG=/path/to/kubeconfig` on Unix systems or `$Env:KUBECONFIG=/path/to/kubeconfig` on Windows.
    2022-03-08 12:22:24,400 INFO  [io.qua.ope.run.AppEventListener] (Quarkus Main Thread) Quarkus Java Operator SDK extension 3.0.2 (commit: 6233ac1 on branch: 6233ac1e71f56d4c52072ab7d2f7cf40591b44d3) built on Mon Jan 24 09:24:35 CET 2022
    2022-03-08 12:22:24,426 INFO  [io.jav.ope.Operator] (Quarkus Main Thread) Operator SDK 2.0.2 is shutting down...
    2022-03-08 12:22:24,427 INFO  [io.qua.it.ope.cli.run.OpenShiftClientProducer] (Quarkus Main Thread) Closing OpenShift client
    2022-03-08 12:22:24,427 ERROR [io.qua.run.Application] (Quarkus Main Thread) Failed to start application (with profile dev): io.javaoperatorsdk.operator.OperatorException: No Controller exists. Exiting!
            at io.javaoperatorsdk.operator.Operator$ControllerManager.shouldStart(Operator.java:159)    
    ```
