package wilda.fr;

import java.io.IOException;
import java.io.InputStream;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.DeleteControl;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;

public class NginxOperatorReconciler implements Reconciler<NginxOperator> {
  private final KubernetesClient client;
  private Watch serviceWatcher;

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

