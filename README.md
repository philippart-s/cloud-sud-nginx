# cloud-sud-nginx
Sources de l'opérateur Nginx pour Cloud Sud 2022

[Slides](https://noti.st/philippart-s/kuH9Z3/developper-un-operateur-kubernetes-en-java-cest-possible)

# Déroulé de la démo
## Initialisation du projet
 - [installer / mettre](https://sdk.operatorframework.io/docs/installation/) à jour la dernière version du [Operator SDK](https://sdk.operatorframework.io/) (v1.18.0 au moment de l'écriture du readme)
 - créer le répertoire `cloud-sud-nginx`: `mkdir cloud-sud-nginx`
 - dans le répertoire `cloud-sud-nginx`, scaffolding du projet avec Quarkus : `operator-sdk init --plugins quarkus --domain fr.wilda --project-name cloud-sud-nginx`
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
## Création de la CRD et de la CR
 - création de l'API : `operator-sdk create api --version v1 --kind NginxOperator`
 - cette commande a créé les 4 classes nécessaires pour créer l'opérateur:
    ```bash
    src
    └── main
        ├── java
        │   └── wilda
        │       └── fr
        │           ├── NginxOperator.java
        │           ├── NginxOperatorReconciler.java
        │           ├── NginxOperatorSpec.java
        │           └── NginxOperatorStatus.java
    ```
  - tester que tout compile que la CRD se génère bien: `mvn clean package` (ou restez en mode `mvn quarkus:dev` pour voir la magie opérer en direct :wink:)
  - pour la CRD doit être générée dans le target, `target/kubernetes/nginxoperators.fr.wilda-v1.yml`:
      ```yaml
      # Generated by Fabric8 CRDGenerator, manual edits might get overwritten!
      apiVersion: apiextensions.k8s.io/v1
      kind: CustomResourceDefinition
      metadata:
        name: nginxoperators.fr.wilda
      spec:
        group: fr.wilda
        names:
          kind: NginxOperator
          plural: nginxoperators
          singular: nginxoperator
        scope: Namespaced
        versions:
        - name: v1
          schema:
            openAPIV3Schema:
              properties:
                spec:
                  type: object
                status:
                  type: object
              type: object
          served: true
          storage: true
          subresources:
            status: {}
      ```
  - changer le paramétrage permettant la création / automatique de la CRD dans le `application.properties`:
      ```properties
      # set to true to automatically apply CRDs to the cluster when they get regenerated
      quarkus.operator-sdk.crd.apply=true
      ```
  - lancer l'opérateur en mode `dev`: `mvn quarkus:dev`:
      ```bash
      2022-03-08 13:46:48,219 WARN  [io.qua.ope.dep.OperatorSDKProcessor] (build-20) 'nginxoperatorreconciler' controller is configured to watch all namespaces, this requires a ClusterRoleBinding for which we MUST specify the namespace of the operator ServiceAccount. This can be specified by setting the 'quarkus.kubernetes.namespace' property. However, as this property is not set, we are leaving the namespace blank to be provided by the user by editing the 'nginxoperatorreconciler-cluster-role-binding' ClusterRoleBinding to provide the namespace in which the operator will be deployed.
      2022-03-08 13:46:48,221 WARN  [io.qua.ope.dep.OperatorSDKProcessor] (build-20) 'nginxoperatorreconciler' controller is configured to validate CRDs, this requires a ClusterRoleBinding for which we MUST specify the namespace of the operator ServiceAccount. This can be specified by setting the 'quarkus.kubernetes.namespace' property. However, as this property is not set, we are leaving the namespace blank to be provided by the user by editing the 'nginxoperatorreconciler-crd-validating-role-binding' ClusterRoleBinding to provide the namespace in which the operator will be deployed.
      __  ____  __  _____   ___  __ ____  ______ 
      --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
      -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
      --\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
      ```
  - vérifier que la CRD a bien été créée : 
      ```bash
      kubectl get crds nginxoperators.fr.wilda
      NAME                      CREATED AT
      nginxoperators.fr.wilda   2022-03-08T12:46:49Z
      ```
  - ajouter un champ `name` dans `NginxOperatorSpec.java`:
      ```java
      public class NginxOperatorSpec {
          private String name;

          public void setName(String name) {
              this.name = name;
          }

          public String getName() {
              return name;
          }
      }
      ```
  - vérifier que la CRD a bien été mise à jour:
      ```bash
      $ kubectl get crds nginxoperators.fr.wilda -o yaml
      apiVersion: apiextensions.k8s.io/v1
      kind: CustomResourceDefinition
      metadata:
        creationTimestamp: "2022-03-08T12:46:49Z"
        generation: 2
        name: nginxoperators.fr.wilda
        resourceVersion: "28080830902"
        uid: acbc5340-292c-4a26-9003-d2d0b9da1683
      spec:
        conversion:
          strategy: None
        group: fr.wilda
        names:
          kind: NginxOperator
          listKind: NginxOperatorList
          plural: nginxoperators
          singular: nginxoperator
        scope: Namespaced
        versions:
        - name: v1
          schema:
            openAPIV3Schema:
              properties:
                spec:
                  properties:
                    name:
                      type: string
                  type: object
      ```
## Le retour du Hello World
Pour finir de valider notre opérateur, créons un Hello World.
 - modifier le reconciler `NginxOperatorReconciler.java`:
    ```java
    public class NginxOperatorReconciler implements Reconciler<NginxOperator> { 
      private final KubernetesClient client;

      public NginxOperatorReconciler(KubernetesClient client) {
        this.client = client;
      }

      @Override
      public UpdateControl<NginxOperator> reconcile(NginxOperator resource, Context context) {

        System.out.println(String.format("Hello %s 🎉🎉 !!", resource.getSpec().getName()));

        return UpdateControl.noUpdate();
      }

      @Override
      public DeleteControl cleanup(NginxOperator resource, Context context) {
        System.out.println(String.format("Goodbye %s 😢", resource.getSpec().getName()));

        return Reconciler.super.cleanup(resource, context);
      }
    }    
    ```
  - créer le namespace `test-helloworld-operator`: `kubectl create ns test-helloworld-operator`
  - créer la CR `src/test/resources/cr-test-hello-world.yaml` pour tester:
      ```yaml
      apiVersion: "fr.wilda/v1"
      kind: NginxOperator
      metadata:
        name: hello-world
      spec:
        name: Cloud Sud 2022      
      ```
  - créer la CR dans Kubernetes : `kubectl apply -f ./src/test/resources/cr-test-hello-world.yaml -n test-helloworld-operator`
  - la sortie de l'opérateur devrait afficher le message `Hello Cloud Sud 2022 🎉🎉 !!`
  - supprimer la CR : `kubectl delete nginxOperator/hello-world -n test-helloworld-operator`
  - la sortie de l'opérateur devrait ressembler à cela:
      ```bash
      Hello Cloud Sud 2022 🎉🎉 !!
      Goodbye Cloud Sud 2022 😢 
      ```
## Gestion du serveur Nginx
 - modifier la classe `NginxOperatorSpec.java`:
      ```java
      public class NginxOperatorSpec {

        private Integer replicaCount;
        private String port;

        public void setPort(String port) {
            this.port = port;
        }

        public String getPort() {
            return port;
        }

        public void setReplicaCount(Integer replicaCount) {
            this.replicaCount = replicaCount;
        }

        public Integer getReplicaCount() {
            return replicaCount;
        }
      }
      ```
 - pour simplifier la création du Pod et du Service pour Nginx on passe par des manifests en YAML.
    `src/main/resources/k8s/nginx-deployment.yaml`:
      ```yaml
      apiVersion: apps/v1
      kind: Deployment
      metadata:
        name: nginx-deployment
        labels:
          app: nginx
      spec:
        replicas: 1
        selector:
          matchLabels:
            app: nginx
        template:
          metadata:
            labels:
              app: nginx
          spec:
            containers:
            - name: nginx
              image: ovhplatform/hello:1.0
              ports:
              - containerPort: 80
      ```
      `src/main/resources/k8s/nginx-service.yaml`:
      ```yaml
      apiVersion: v1
      kind: Service
      metadata:
        name: "nginx-service"
      spec:
        selector:
          app: "nginx"
        ports:
        - name: http
          protocol: TCP
          port: 80
          targetPort: 80
        type: LoadBalancer
     ```
 - modifier le reconciler `NginxOperatorReconciler.java`:
    ```java
    public class NginxOperatorReconciler implements Reconciler<NginxOperator> {
      private final KubernetesClient client;

      public NginxOperatorReconciler(KubernetesClient client) {
        this.client = client;
      }

      @Override
      public UpdateControl<NginxOperator> reconcile(NginxOperator resource, Context context) {

        System.out.println("🛠️  Create / update Nginx resource operator ! 🛠️");

        String namespace = resource.getMetadata().getNamespace();

        // Load the Nginx deployment
        Deployment deployment = loadYaml(Deployment.class, "/k8s/nginx-deployment.yaml");
        // Apply the number of replicas and namespace
        deployment.getSpec().setReplicas(resource.getSpec().getReplicaCount());
        deployment.getMetadata().setNamespace(namespace);

        // Create or update Nginx server
        client.apps().deployments().inNamespace(namespace).createOrReplace(deployment);

        // Create service
        Service service = loadYaml(Service.class, "/k8s/nginx-service.yaml");
        service.getSpec().getPorts().get(0).setPort(resource.getSpec().getPort());
        client.services().inNamespace(namespace).createOrReplace(service);
        
        return UpdateControl.noUpdate();
      }

      @Override
      public DeleteControl cleanup(NginxOperator resource, Context context) {
        System.out.println("💀 Delete Nginx resource operator ! 💀");

        client.apps().deployments().inNamespace(resource.getMetadata().getNamespace()).delete();
        client.services().inNamespace(resource.getMetadata().getNamespace()).withName("nginx-service").delete();

        return Reconciler.super.cleanup(resource, context);
      }

      /**
      * Load a YAML file and transform it to a Java class.
      * 
      * @param clazz The java class to create
      * @param yamlPath The yaml file path in the classpath
      */
      private <T> T loadYaml(Class<T> clazz, String yamlPath) {
        try (InputStream is = getClass().getResourceAsStream(yamlPath)) {
          return Serialization.unmarshal(is, clazz);
        } catch (IOException ex) {
          throw new IllegalStateException("Cannot find yaml on classpath: " + yamlPath);
        }
      }
    }
    ```
 - créer le namespace `test-nginx-operator`: `kubectl create ns test-nginx-operator`
 - créer la CR: `src/test/resources/cr-test-nginx-operator.yaml`:
      ```yaml
      apiVersion: "fr.wilda/v1"
      kind: NginxOperator
      metadata:
        name: nginx-cloud-sud
      spec:
        replicaCount: 1
        port: 80
      ```
 - puis l'appliquer sur Kubernetes: `kubectl apply -f ./src/test/resources/cr-test-nginx-operator.yaml -n test-nginx-operator`
 - l'opérateur devrait créer le pod Nginx et son service associé:
      Dans le terminal du quarkus:
      ```bash
      🛠️  Create / update Nginx resource operator ! 🛠️
      ```
      Dans Kubernetes:
      ```bash
      $ kubectl get pod,svc  -n test-nginx-operator

      NAME                                    READY   STATUS    RESTARTS   AGE
      pod/nginx-deployment-84c7b56775-kzsjq   1/1     Running   0          110s

      NAME                    TYPE           CLUSTER-IP     EXTERNAL-IP     PORT(S)        AGE
      service/nginx-service   LoadBalancer   10.3.108.159   51.XXX.XXX.178   80:30751/TCP   110s      
      ```
 - tester dans un navigateur ou par un curl l'accès à `http://51.XXX.XXX.178`
 - changer le port et le nombre de replicas dans la CR `cr-test-nginx-operator.yaml`:
      ```yaml
      apiVersion: "fr.wilda/v1"
      kind: NginxOperator
      metadata:
        name: nginx-cloud-sud
      spec:
        replicaCount: 2
        port: 8080
      ```
 - appliquer la CR: `kubectl apply -f ./src/test/resources/cr-test-nginx-operator.yaml -n test-nginx-operator`
 - vérifier que le nombre de pods et le port ont bien changés:
    ```bash
    $ kubectl get pod,svc  -n test-nginx-operator

    NAME                                    READY   STATUS    RESTARTS   AGE
    pod/nginx-deployment-84c7b56775-7w6jp   1/1     Running   0          4m59s
    pod/nginx-deployment-84c7b56775-kzsjq   1/1     Running   0          19m

    NAME                    TYPE           CLUSTER-IP     EXTERNAL-IP     PORT(S)          AGE
    service/nginx-service   LoadBalancer   10.3.108.159   51.XXX.XXX.178   8080:30751/TCP   19m
    ```
 - tester dans un navigateur ou par un curl l'accès à `http://51.XXX.XXX.178:8080`

## Un Ops dans le moteur
 - supprimer le service: `kubectl delete svc/nginx-service -n test-nginx-operator`
 - vérifier qu'il n'est pas recréé:
    ```bash
    $ kubectl get svc  -n test-nginx-operator

    No resources found in test-nginx-operator namespace.
    ```
 - recréer le service : `kubectl apply -f ./src/main/resources/k8s/nginx-service.yaml -n test-nginx-operator`
 - modifier le reconciler `NginxOperatorReconciler.java` pour qu'il surveille le service:
    ```java
    public UpdateControl<NginxOperator> reconcile(NginxOperator resource, Context context) {

        System.out.println("🛠️  Create / update Nginx resource operator ! 🛠️");

        String namespace = resource.getMetadata().getNamespace();

        // Load the Nginx deployment
        Deployment deployment = loadYaml(Deployment.class, "/k8s/nginx-deployment.yaml");
        // Apply the number of replicas and namespace
        deployment.getSpec().setReplicas(resource.getSpec().getReplicaCount());
        deployment.getMetadata().setNamespace(namespace);

        // Create or update Nginx server
        client.apps().deployments().inNamespace(namespace).createOrReplace(deployment);

        // Create service
        Service service = loadYaml(Service.class, "/k8s/nginx-service.yaml");
        service.getSpec().getPorts().get(0).setPort(resource.getSpec().getPort());
        client.services().inNamespace(namespace).createOrReplace(service);

        // Watch if the service is deleted: recreate it
        client.services().inNamespace(namespace).watch(new Watcher<Service>() {
          @Override
          public void eventReceived(Action action, Service resource) {
            System.out.println("⚡ Event receive on watcher ! ⚡ ➡️ " + action.name());

            if (action == Action.DELETED) {
              System.out.println("🗑️  Service deleted, recreate it ! 🗑️");

              client.services().inNamespace(namespace).createOrReplace(service);
            }
          }

          @Override
          public void onClose(WatcherException cause) {
            System.out.println("☠️ Watcher closed due to unexpected error : " + cause);

            // To get ride of error : io.fabric8.kubernetes.client.WatcherException: too old resource version: 28129827227 (28130338369)
            // Either set a flag to recreate a watcher
            // Or use SharedInformerFactory : https://stackoverflow.com/a/61437982

          }
        });

        return UpdateControl.noUpdate();
      }
    ```
- supprimer le service: `kubectl delete svc/nginx-service -n test-nginx-operator`
- l'opérateur le recrée:
    ```bash
    Service deleted, recreate it ! 🗑️
    ⚡ Event receive on watcher ! ⚡ ➡️ ADDED
    ⚡ Event receive on watcher ! ⚡ ➡️ MODIFIED    
    ```
 - supprimer la CR: `kubectl delete nginxOperator/nginx-cloud-sud -n test-nginx-operator`
 - constater que l'opérateur recrée le service:
    ```bash
    💀 Delete Nginx resource operator ! 💀
    ⚡ Event receive on watcher ! ⚡ ➡️ MODIFIED
    ⚡ Event receive on watcher ! ⚡ ➡️ DELETED
    🗑️  Service deleted, recreate it ! 🗑️
    ⚡ Event receive on watcher ! ⚡ ➡️ ADDED
    ⚡ Event receive on watcher ! ⚡ ➡️ MODIFIED
    ```
## Eviter le jour de la marmotte
 - modifier le reconciler `NginxOperatorReconciler.java`:
    ```java
    public class NginxOperatorReconciler implements Reconciler<NginxOperator> {
      private final KubernetesClient client;
      private Watch serviceWatcher;

      // ...

      @Override
      public UpdateControl<NginxOperator> reconcile(NginxOperator resource, Context context) {

        System.out.println("🛠️  Create / update Nginx resource operator ! 🛠️");

        // ...

        // Watch if the service is deleted: recreate it
        serviceWatcher = client.services().inNamespace(namespace).watch(new Watcher<Service>() {
          @Override
          public void eventReceived(Action action, Service resource) {
            System.out.println("⚡ Event receive on watcher ! ⚡ ➡️ " + action.name());

            if (action == Action.DELETED) {
              System.out.println("🗑️  Service deleted, recreate it ! 🗑️");

              client.services().inNamespace(namespace).createOrReplace(service);
            }
          }

          @Override
          public void onClose(WatcherException cause) {
            System.out.println("☠️ Watcher closed due to unexpected error : " + cause);

            // To get ride of error : io.fabric8.kubernetes.client.WatcherException: too old resource
            // version: 28129827227 (28130338369)
            // Either set a flag to recreate a watcher
            // Or use SharedInformerFactory : https://stackoverflow.com/a/61437982
          }
        });

        return UpdateControl.noUpdate();
      }

      @Override
      public DeleteControl cleanup(NginxOperator resource, Context context) {
        System.out.println("💀 Delete Nginx resource operator ! 💀");

        // To avoid the automatic recreation
        if (serviceWatcher != null) serviceWatcher.close();

        client.apps().deployments().inNamespace(resource.getMetadata().getNamespace()).delete();
        client.services().inNamespace(resource.getMetadata().getNamespace()).withName("nginx-service")
            .delete();

        return Reconciler.super.cleanup(resource, context);
      }

      // ...
    }    
    ```
 - créer la CR `cr-test-nginx-operator.yaml`: `kubectl apply -f ./src/test/resources/cr-test-nginx-operator.yaml -n test-nginx-operator`
 - supprimer la CR: `kubectl delete nginxOperator/nginx-cloud-sud -n test-nginx-operator`
 - l'opérateur ne recrée pas le service:
    ```bash
    🛠️  Create / update Nginx resource operator ! 🛠️
    ⚡ Event receive on watcher ! ⚡ ➡️ ADDED
    ⚡ Event receive on watcher ! ⚡ ➡️ MODIFIED
    💀 Delete Nginx resource operator ! 💀    
    ```
 - vérifier que tout a été supprimé: 
    ```bash
    $ kubectl get svc  -n test-nginx-operator
    No resources found in test-nginx-operator namespace.    
    ```
## Packaging et déploiement
 - modifier le fichier `application.properties`:
    ```properties
    quarkus.container-image.build=true
    quarkus.container-image.push=false
    quarkus.container-image.group=wilda
    quarkus.container-image.name=cloud-sud-nginx-operator

    # set to true to automatically apply CRDs to the cluster when they get regenerated
    quarkus.operator-sdk.crd.apply=true
    # set to true to automatically generate CSV from your code
    quarkus.operator-sdk.generate-csv=false

    quarkus.log.level=INFO

    quarkus.kubernetes.namespace=cloud-sud-nginx-operator
    ```
 - ajouter un fichier `src/main/kubernetes/kubernetes.yml` contenant la définition des _ClusterRole_ / _ClusterRoleBinding_ spécifiques à l'opérateur:
    ```yaml
    apiVersion: rbac.authorization.k8s.io/v1
    kind: ClusterRole
    metadata:
      name: service-deployment-cluster-role
      namespace: cloud-sud-nginx-operator
    rules:
      - apiGroups:
        - ""
        resources:
        - secrets
        - serviceaccounts
        - services  
        verbs:
        - "*"
      - apiGroups:
        - "apps"
        verbs:
          - "*"
        resources:
        - deployments
    ---
    apiVersion: rbac.authorization.k8s.io/v1
    kind: ClusterRoleBinding
    metadata:
      name: service-deployment-cluster-role-binding
      namespace: cloud-sud-nginx-operator
    roleRef:
      kind: ClusterRole
      apiGroup: rbac.authorization.k8s.io
      name: service-deployment-cluster-role
    subjects:
      - kind: ServiceAccount
        name: cloud-sud-nginx-operator
        namespace: cloud-sud-nginx-operator
    ---
    ```
- lancer le packaging : `mvn clean package`
- vérifier que l'image a bien été générée: : `docker images | grep cloud-sud`:
    ```bash
    wilda/cloud-sud-nginx-operator          0.0.1-SNAPSHOT         97dac3e852da   5 minutes ago   232MB
    ```
- push de l'image : `docker login` && `docker push wilda/cloud-sud-nginx-operator:0.0.1-SNAPSHOT`
- créer le namespace `cloud-sud-nginx-operator`: `kubectl create ns cloud-sud-nginx-operator`
- si nécessaire créer la CRD: `kubectl apply -f ./target/kubernetes/nginxoperators.fr.wilda-v1.yml`
- appliquer le manifest créé : `kubectl apply -f ./target/kubernetes/kubernetes.yml`
- vérifier que tout va bien:
    ```bash
    $ kubectl get pod -n cloud-sud-nginx-operator

    NAME                                        READY   STATUS    RESTARTS   AGE
    cloud-sud-nginx-operator-5649886754-5lgd5   1/1     Running   0          2m15s    

    $ kubectl logs cloud-sud-nginx-operator-5649886754-5lgd5 -n cloud-sud-nginx-operator
    __  ____  __  _____   ___  __ ____  ______ 
    --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
    -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
    --\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
    2022-03-09 14:37:17,922 INFO  [io.jav.ope.Operator] (main) Registered reconciler: 'nginxoperatorreconciler' for resource: 'class wilda.fr.NginxOperator' for namespace(s): [all namespaces]
    2022-03-09 14:37:17,932 INFO  [io.qua.ope.run.AppEventListener] (main) Quarkus Java Operator SDK extension 3.0.2 (commit: 6233ac1 on branch: 6233ac1e71f56d4c52072ab7d2f7cf40591b44d3) built on Mon Jan 24 08:24:35 GMT 2022
    2022-03-09 14:37:17,932 INFO  [io.jav.ope.Operator] (main) Operator SDK 2.0.2 (commit: ba03d9e) built on Thu Jan 20 10:22:23 GMT 2022 starting...
    2022-03-09 14:37:17,933 INFO  [io.jav.ope.Operator] (main) Client version: 5.11.2
    2022-03-09 14:37:18,702 INFO  [io.quarkus] (main) cloud-sud-nginx 0.0.1-SNAPSHOT on JVM (powered by Quarkus 2.6.3.Final) started in 2.708s. Listening on: http://0.0.0.0:8080
    2022-03-09 14:37:18,702 INFO  [io.quarkus] (main) Profile prod activated. 
    2022-03-09 14:37:18,703 INFO  [io.quarkus] (main) Installed features: [cdi, kubernetes, kubernetes-client, micrometer, openshift-client, operator-sdk, smallrye-context-propagation, smallrye-health, vertx]
    ```
- tester l'opérateur en créant une CR: `kubectl apply -f ./src/test/resources/cr-test-nginx-operator.yaml -n test-nginx-operator`
- puis en la supprimant: `kubectl delete nginxOperator/nginx-cloud-sud -n test-nginx-operator`
- et constater que tout va bien:
```bash
  🛠️  Create / update Nginx resource operator ! 🛠️                                  
  ⚡ Event receive on watcher ! ⚡ ➡️ ADDED
  💀 Delete Nginx resource operator ! 💀            
```
- supprimer l'opérateur si souhaité: `kubectl delete -f ./target/kubernetes/kubernetes.yml`
