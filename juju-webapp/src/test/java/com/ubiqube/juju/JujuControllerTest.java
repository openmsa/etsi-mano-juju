package com.ubiqube.juju;

import com.ubiqube.etsi.mano.service.juju.entities.JujuCloud;
import com.ubiqube.etsi.mano.service.juju.entities.JujuCredential;
import com.ubiqube.etsi.mano.service.juju.entities.JujuMetadata;
import com.ubiqube.juju.controller.JujuController;
import com.ubiqube.juju.service.ProcessResult;
import com.ubiqube.juju.service.WorkspaceService;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JujuControllerTest {

@Mock
JujuCloud jujuCloud;

@Mock
JujuMetadata jujuMetadata;

@Mock
WorkspaceService ws;

@InjectMocks
JujuController jujuController;
    @Test
    public void test_addCloud() throws Exception {

        ProcessResult res = ProcessResult.builder().exitCode(0).stdout("").errout("cloud \"openstack-inari-test\" successfully added to your local client.\n" +
                "You will need to add a credential for this cloud (juju add-credential openstack-inari-test)\n" +
                "before you can use it to bootstrap a controller (juju bootstrap openstack-inari-test) or\n" +
                "to create a model (juju add-model <your model name> openstack-inari-test).").build();

        Mockito.when(ws.addCloud(Mockito.any(),Mockito.any())).thenReturn(res);
        jujuController.addCloud(jujuCloud);
        assertTrue(true);
    }

    @Test
    public void test_addCloud_whenJujuExceptionThrows() throws Exception {

        ProcessResult res = ProcessResult.builder().exitCode(1).stdout("").errout("ERROR use update-cloud openstack-inari-test --client to override known definition: local cloud \"openstack-inari-test\" already exists").build();

        Mockito.when(ws.addCloud(Mockito.any(),Mockito.any())).thenReturn(res);

        Exception exception = assertThrows(JujuException.class, () -> {
                      jujuController.addCloud(jujuCloud);
                 });

        String expectedMsg = "openstack-inari-test";
        String actualMsg = exception.getMessage();
        assertTrue(actualMsg.contains(expectedMsg));
    }

   @Test
   public void test_getClouds() throws Exception {

       ProcessResult res = ProcessResult.builder().exitCode(0).stdout("You can bootstrap a new controller using one of these clouds...\n" +
               "\n" +
               "Clouds available on the client:\n" +
               "Cloud                        Regions     Default    Type       Credentials        Source    Description\n" +
               "openstack-inari-test            1        RegionOne  openstack        0            local").errout("Only clouds with registered credentials are shown.\n" +
               "There are more clouds, use --all to see them.").build();

       Mockito.when(ws.addCloud(Mockito.any(),Mockito.any())).thenReturn(res);
       jujuController.addCloud(jujuCloud);
       Mockito.when(ws.clouds()).thenReturn(res);
       jujuController.clouds();
       assertTrue(true);
    }

    @Test
    public void test_getCloudDetail(){

        ProcessResult res = ProcessResult.builder().exitCode(0).stdout("Client cloud \"openstack-inari-test\":\n" +
                "\n" +
                "defined: local\n" +
                "type: openstack\n" +
                "description: Openstack Cloud\n" +
                "auth-types: [userpass]\n" +
                "regions:\n" +
                "  RegionOne:\n" +
                "    endpoint: http://10.31.1.108:5000/v3").errout("").build();

        Mockito.when(ws.cloudDetail(Mockito.anyString())).thenReturn(res);
        jujuController.cloudDetail("openstack-inari-test");
        assertTrue(true);
    }

    @Test
    public void test_getCloudDetail_whenJujuExceptionThrows() throws Exception {

        ProcessResult res = ProcessResult.builder().exitCode(1).stdout("").errout("ERROR cloud openstack-inari-test not found, possible alternative clouds:\n" +
                "\n" +
                "  - aws\n" +
                "  - aws-china\n" +
                "  - aws-gov\n" +
                "  - azure").build();

        Mockito.when(ws.cloudDetail(Mockito.anyString())).thenReturn(res);

        Exception exception = assertThrows(JujuException.class, () -> {
            jujuController.cloudDetail("openstack-inari-test");
        });

        String expectedMsg = "openstack-inari-test";
        String actualMsg = exception.getMessage();
        assertTrue(actualMsg.contains(expectedMsg));
    }

    @Test
    public void test_deleteCloud(){

        ProcessResult res = ProcessResult.builder().exitCode(0).stdout("").errout("").build();
        ProcessResult res2 = ProcessResult.builder().exitCode(0).stdout("").errout("Removed details of cloud openstack-inari-test from this client").build();

        Mockito.when(ws.cloudDetail("openstack-inari-test")).thenReturn(res);
        Mockito.when(ws.removeCloud(Mockito.anyString())).thenReturn(res2);

        jujuController.removeCloud("openstack-inari-test");
        assertTrue(true);
    }

    @Test
    public void test_deleteCloud_whenJujuExceptionThrows(){

        ProcessResult res = ProcessResult.builder().exitCode(1).stdout("").errout("ERROR cloud openstack-inari-test not found, possible alternative clouds:\\n\" +\n" +
                "                \"\\n\" +\n" +
                "                \"  - aws\\n\" +\n" +
                "                \"  - aws-china\\n\" +\n" +
                "                \"  - aws-gov\\n\" +\n" +
                "                \"  - azure\"").build();
        ProcessResult res2 = ProcessResult.builder().exitCode(0).stdout("").errout("No cloud called \"openstack-inari-test\" exists on this client").build();

        Mockito.when(ws.cloudDetail("openstack-inari-test")).thenReturn(res);
        Mockito.when(ws.removeCloud("openstack-inari-test")).thenReturn(res2);
        Exception exception = assertThrows(JujuException.class, () -> {
            jujuController.removeCloud("openstack-inari-test");
        });

        String expectedMsg = "openstack-inari-test";
        String actualMsg = exception.getMessage();
        assertTrue(actualMsg.contains(expectedMsg));
    }

    @Test
    public void test_addCredential() throws Exception {

        ProcessResult res = ProcessResult.builder().exitCode(0).stdout("Credential \"inari-admin-tenanw\" added locally for cloud \"openstack-inari-test\".").errout("").build();
        JujuCredential jujuCredential1 = JujuCredential.builder().name("inari-admin-tenanw").authType("userpass").username("admin").password("9f865149f0b64c92").tenantName("admin").build();
        JujuCloud jujuCloud1 = JujuCloud.builder().name("openstack-inari-test").type("openstack").authTypes("[userpass]").credential(jujuCredential1).build();
        Mockito.when(ws.addCredential(Mockito.anyString(),Mockito.anyString())).thenReturn(res);

        jujuController.addCredential(jujuCloud1);
        assertTrue(true);
    }

    @Test
    public void test_addCredential_whenJujuExceptionThrows() throws Exception {

        ProcessResult res = ProcessResult.builder().exitCode(1).stdout("No local credentials for cloud \"openstack-inari-test\" changed.").errout("ERROR credential \"inari-admin-tenanw\" for cloud \"openstack-inari-test\" already exists locally, use 'juju update-credential openstack-inari-test inari-admin-tenanw -f mycreds.yaml' to update this local client copy").build();
        JujuCredential jujuCredential1 = JujuCredential.builder().name("inari-admin-tenanw").authType("userpass").username("admin").password("9f865149f0b64c92").tenantName("admin").build();
        JujuCloud jujuCloud1 = JujuCloud.builder().name("openstack-inari-test").type("openstack").authTypes("[userpass]").credential(jujuCredential1).build();

        Mockito.when(ws.addCredential(Mockito.anyString(),Mockito.anyString())).thenReturn(res);

        Exception exception = assertThrows(JujuException.class, () -> {
            jujuController.addCredential(jujuCloud1);
        });

        String expectedMsg = "openstack-inari-test";
        String actualMsg = exception.getMessage();
        assertTrue(actualMsg.contains(expectedMsg));
    }

    @Test
    public void test_getCredentials() throws Exception {

        ProcessResult res = ProcessResult.builder().exitCode(0).stdout("No credentials from any controller to display.\n" +
                "\n" +
                "Client Credentials:\n" +
                "Cloud                Credentials\n" +
                "openstack-inari-108test  inari-admin-tenanttest").errout("").build();
     
        Mockito.when(ws.credentials()).thenReturn(res);
        jujuController.credentials();
        assertTrue(true);
    }

    @Test
    public void test_getCredentialDetail(){

        ProcessResult res2 = ProcessResult.builder().exitCode(0).stdout("Client cloud \"openstack-inari-test\":\n" +
                "\n" +
                "defined: local\n" +
                "type: openstack\n" +
                "description: Openstack Cloud\n" +
                "auth-types: [userpass]\n" +
                "credential-count: 1\n" +
                "regions:\n" +
                "  RegionOne:\n" +
                "    endpoint: http://10.31.1.108:5000/v3").errout("").build();
        ProcessResult res = ProcessResult.builder().exitCode(0).stdout("client-credentials:\n" +
                "  openstack-inari-test:\n" +
                "    inari-admin-tenanw:\n" +
                "      content:\n" +
                "        auth-type: userpass\n" +
                "        tenant-name: admin\n" +
                "        username: admin").errout("").build();


        Mockito.when(ws.credentialDetail(Mockito.anyString(),Mockito.anyString())).thenReturn(res);
        Mockito.when(ws.cloudDetail(Mockito.anyString())).thenReturn(res2);
        jujuController.credentialDetails("openstack-inari-test","inari-admin-tenanw");
        assertTrue(true);
    }

    @Test
    public void test_getCredentialDetail_whenJujuExceptionthrows_without_Cloud(){

        ProcessResult res = ProcessResult.builder().exitCode(0).stdout("").errout("ERROR client credential content lookup failed: loading credentials: credentials for cloud openstack-inari-test2 not found\n" +
                "No credentials from this client or from a controller to display.").build();

        ProcessResult res2 = ProcessResult.builder().exitCode(1).stdout("").errout("ERROR cloud openstack-inari-test2 not found, possible alternative clouds:\n" +
                "\n" +
                "  - aws\n" +
                "  - aws-china\n" +
                "  - aws-gov\n" +
                "  - azure\n" +
                "  - azure-china").build();


        Mockito.when(ws.credentialDetail(Mockito.anyString(),Mockito.anyString())).thenReturn(res2);
        Mockito.when(ws.cloudDetail(Mockito.anyString())).thenReturn(res);

        Exception exception = assertThrows(JujuException.class, () -> {
            jujuController.credentialDetails("openstack-inari-test2","inari-admin-tenanw");
        });

        String expectedMsg = "openstack-inari-test2";
        String actualMsg = exception.getMessage();
        assertTrue(actualMsg.contains(expectedMsg));

    }

    @Test
    public void test_getCredentialDetail_whenJujuExceptionthrows_without_Credential_in_cloud(){

        ProcessResult res = ProcessResult.builder().exitCode(0).stdout("").errout("ERROR client credential content lookup failed: loading credentials: credentials for cloud openstack-inari-test2 not found\n" +
                "No credentials from this client or from a controller to display.").build();

        ProcessResult res2 = ProcessResult.builder().exitCode(0).stdout("client cloud \"openstack-inari-test2\":\n" +
                "\n" +
                "defined: local\n" +
                "type: openstack\n" +
                "description: Openstack Cloud\n" +
                "auth-types: [userpass]\n" +
                "regions:\n" +
                "  RegionOne:\n" +
                "    endpoint: http://10.31.1.108:5000/v3\n").errout("").build();


        Mockito.when(ws.credentialDetail(Mockito.anyString(),Mockito.anyString())).thenReturn(res);
        Mockito.when(ws.cloudDetail(Mockito.anyString())).thenReturn(res2);

        Exception exception = assertThrows(JujuException.class, () -> {
            jujuController.credentialDetails("openstack-inari-test2","inari-admin-tenanw");
        });

        String expectedMsg = "openstack-inari-test2";
        String actualMsg = exception.getMessage();
        assertTrue(actualMsg.contains(expectedMsg));
    }

    @Test
    public void test_updateCredential() throws Exception {

        ProcessResult res = ProcessResult.builder().exitCode(0).stdout("").errout("Local client was updated successfully with provided credential information.").build();
        
        JujuCredential jujuCredential1 = JujuCredential.builder().name("inari-admin-tenanw").authType("userpass").username("admintest").password("9f865149f0b64c92").tenantName("admin").build();
        JujuCloud jujuCloud1 = JujuCloud.builder().name("openstack-inari-test").type("openstack").authTypes("[userpass]").credential(jujuCredential1).build();
        Mockito.when(ws.updateCredential(Mockito.anyString(),Mockito.anyString())).thenReturn(res);

        jujuController.updateCredential(jujuCloud1);
        assertTrue(true);
    }

    @Test
    public void test_update_Credential_whenJujuExceptionThrows() throws Exception {

        ProcessResult res = ProcessResult.builder().exitCode(1).stdout("").errout("Cloud \"openstack-inari-test\" not found.").build();
        
        JujuCredential jujuCredential1 = JujuCredential.builder().name("inari-admin-tenanw").authType("userpass").username("admin").password("9f865149f0b64c92").tenantName("admin").build();
        JujuCloud jujuCloud1 = JujuCloud.builder().name("openstack-inari-test").type("openstack").authTypes("[userpass]").credential(jujuCredential1).build();

        Mockito.when(ws.updateCredential(Mockito.anyString(),Mockito.anyString())).thenReturn(res);

        Exception exception = assertThrows(JujuException.class, () -> {
            jujuController.updateCredential(jujuCloud1);
        });

        String expectedMsg = "openstack-inari-test";
        String actualMsg = exception.getMessage();
        assertTrue(actualMsg.contains(expectedMsg));
    }

    @Test
    public void test_deleteCredentail_with_cloud_with_credential(){

        ProcessResult res = ProcessResult.builder().exitCode(0).stdout("").errout("").build();
        ProcessResult res2 = ProcessResult.builder().exitCode(0).stdout("test").errout("").build();
        ProcessResult res3 = ProcessResult.builder().exitCode(0).stdout("").errout("Found local cloud \"openstack-inari-test2\" on this client.\n" +
                "Credential \"inari-admin-tenanw\" for cloud \"openstack-inari-test2\" removed from this client.").build();

        Mockito.when(ws.cloudDetail("openstack-inari-test2")).thenReturn(res);
        Mockito.when(ws.credentialDetail(Mockito.anyString(),Mockito.anyString())).thenReturn(res2);
        Mockito.when(ws.removeCredential(Mockito.anyString(),Mockito.anyString())).thenReturn(res3);

        jujuController.removeCredential("openstack-inari-test2","inari-admin-tenanw");
        assertTrue(true);
    }

    @Test
    public void test_deleteCredential_whenJujuExceptionthrows_without_Cloud(){

        ProcessResult res1 = ProcessResult.builder().exitCode(1).stdout("").errout("ERROR cloud openstack-inari-test2 not found, possible alternative clouds:\n" +
                "\n" +
                "  - aws\n" +
                "  - aws-china\n" +
                "  - aws-gov\n" +
                "  - azure\n" +
                "  - azure-china").build();

        ProcessResult res2 = ProcessResult.builder().exitCode(0).stdout("").errout("ERROR client credential content lookup failed: loading credentials: credentials for cloud openstack-inari-test2 not found\n" +
                "No credentials from this client or from a controller to display.").build();

        Mockito.when(ws.cloudDetail(Mockito.anyString())).thenReturn(res1);
        Mockito.when(ws.credentialDetail(Mockito.anyString(),Mockito.anyString())).thenReturn(res2);


        Exception exception = assertThrows(JujuException.class, () -> {
            jujuController.removeCredential("openstack-inari-test2","inari-admin-tenanw");
        });

        String expectedMsg = "openstack-inari-test2";
        String actualMsg = exception.getMessage();
        assertTrue(actualMsg.contains(expectedMsg));

    }

    @Test
    public void test_deleteCredential_whenJujuExceptionthrows_without_Credential(){

        ProcessResult res1 = ProcessResult.builder().exitCode(0).stdout("Client cloud \"openstack-inari-test2\":\n" +
                "\n" +
                "defined: local\n" +
                "type: openstack\n" +
                "description: Openstack Cloud\n" +
                "auth-types: [userpass]\n" +
                "regions:\n" +
                "  RegionOne:\n" +
                "    endpoint: http://10.31.1.108:5000/v3").errout("").build();

        ProcessResult res2 = ProcessResult.builder().exitCode(0).stdout("").errout("ERROR client credential content lookup failed: loading credentials: credentials for cloud openstack-inari-test2 not found\n" +
                "No credentials from this client or from a controller to display.").build();

        Mockito.when(ws.cloudDetail(Mockito.anyString())).thenReturn(res1);
        Mockito.when(ws.credentialDetail(Mockito.anyString(),Mockito.anyString())).thenReturn(res2);


        Exception exception = assertThrows(JujuException.class, () -> {
            jujuController.removeCredential("openstack-inari-test2","inari-admin-tenanw");
        });
        String expectedMsg = "openstack-inari-test2";
        String actualMsg = exception.getMessage();
        assertTrue(actualMsg.contains(expectedMsg));

    }

    @Test
    public void test_add_Controllers() throws Exception {

        ProcessResult res = ProcessResult.builder().exitCode(0).stdout("").errout("Bootstrap complete, controller \"openstack-inari-test-controller\" is now available\n"
        		+ "Controller machines are in the \"controller\" model\n"
        		+ "\n"
        		+ "Now you can run\n"
        		+ "        juju add-model <model-name>\n"
        		+ "to create a new model to deploy workloads.\n"
        		+ "").build();

        Mockito.when(ws.addController(Mockito.anyString(),Mockito.any())).thenReturn(res);
        jujuController.addController("openstack-inari-testcloud",jujuMetadata);
        assertTrue(true);
     }
    
    @Test
    public void test_add_Controllers_when_JujuException_throws() throws Exception {

        ProcessResult res = ProcessResult.builder().exitCode(1).stdout("").errout("ERROR unknown cloud \"openstack-inari-testcloud\", please try \"juju update-public-clouds\"\n"
        		+ "").build();

        Mockito.when(ws.addController(Mockito.anyString(),Mockito.any())).thenReturn(res);
        
        Exception exception = assertThrows(JujuException.class, () -> {
        	jujuController.addController("openstack-inari-testcloud",jujuMetadata);
        	});

        String expectedMsg = "openstack-inari-testcloud";
        String actualMsg = exception.getMessage();
        assertTrue(actualMsg.contains(expectedMsg));
     }
    
    @Test
    public void test_getControllers() throws Exception {

        ProcessResult res = ProcessResult.builder().exitCode(0).stdout("controllers\":{\"openstack-inari-test-controller\":{\"user\":\"admin\",\"access\":\"superuser\",\"recent-server\":\"10.31.1.49:17070\",\"uuid\":\"793d364b-8939-47e3-8425-69657005f945\",\"api-endpoints\":[\"10.31.1.49:17070\",\"252.49.0.1:17070\"],\"ca-ce---BEGIN CERTIFICATECAnugAwIBAgIVAOB8OyWi33A3kXq7srlxor95CTT6MA0GCSqGSIb3DQEB\\nCwUAMCExDTALBgNVBAoTBEp1").errout("").build();

        Mockito.when(ws.controllers()).thenReturn(res);
        jujuController.controllers();
        assertTrue(true);
     }
    
    @Test
    public void test_getControllerDetail(){

        ProcessResult res = ProcessResult.builder().exitCode(0).stdout("openstack-inari-test-controller:\n"
        		+ "  details:\n"
        		+ "    controller-uuid: 793d364b-8939-47e3-8425-69657005f945\n"
        		+ "    api-endpoints: ['10.31.1.49:17070', '252.49.0.1:17070']\n"
        		+ "    cloud: openstack-inari-108\n"
        		+ "    region: RegionOne\n"
        		+ "    agent-version: 3.2.3\n"
        		+ "").errout("").build();

        Mockito.when(ws.showController(Mockito.anyString())).thenReturn(res);
        jujuController.controllerDetail("openstack-inari-test-controller");
        assertTrue(true);
    }

    @Test
    public void test_getControllerDetail_when_JujuException_Throws_No_controller(){

        ProcessResult res = ProcessResult.builder().exitCode(1).stdout("").errout("ERROR controller openstack-inari-test-controller not found\\n").build();

        Mockito.when(ws.showController(Mockito.anyString())).thenReturn(res);      
        
        Exception exception = assertThrows(JujuException.class, () -> {
        	jujuController.controllerDetail("openstack-inari-test-controller");    
        	});

        String expectedMsg = "openstack-inari-test-controller";
        String actualMsg = exception.getMessage();
        assertTrue(actualMsg.contains(expectedMsg));

    }
    
    @Test
    public void test_deleteController(){

        ProcessResult res = ProcessResult.builder().exitCode(0).stdout("").errout("WARNING This command will destroy the \"openstack-inari-test-controller\" controller and all its resources\n"
        		+ "Destroying controller\n"
        		+ "Waiting for model resources to be reclaimed\n"
        		+ "Waiting for 2 models, 1 application\n"
        		+ "All models reclaimed, cleaning up controller machines\n"
        		+ "").build();

              
        Mockito.when(ws.removeController(Mockito.anyString())).thenReturn(res);
        
        	jujuController.removeController("openstack-inari-test-controller");    
        	assertTrue(true);
    }

    
    @Test
    public void test_deleteController_when_JujuException_Throws_No_controller(){

        ProcessResult res = ProcessResult.builder().exitCode(1).stdout("").errout("ERROR controller openstack-inari-test-controller not found\\n").build();

        Mockito.when(ws.removeController(Mockito.anyString())).thenReturn(res);      
        
        Exception exception = assertThrows(JujuException.class, () -> {
        	jujuController.removeController("openstack-inari-test-controller");    
        	});

        String expectedMsg = "openstack-inari-test-controller";
        String actualMsg = exception.getMessage();
        assertTrue(actualMsg.contains(expectedMsg));

    }
    
    @Test
    public void test_addModel(){

        ProcessResult res = ProcessResult.builder().exitCode(0).stdout("").errout("Added 'testmodel' model on openstack-inari-test/RegionOne with credential 'inari-admin-tenant' for user 'admin'\n"
        		+ "").build();
        
        Mockito.when(ws.addModel("testmodel")).thenReturn(res);
        jujuController.addModel("testmodel");
        assertTrue(true);
    }

    @Test
    public void test_addModel_when_JujuException_throws(){

        ProcessResult res = ProcessResult.builder().exitCode(1).stdout("").errout("ERROR failed to create new model: model \"testmodel\" for admin already exists (already exists)\n"
        		+ "").build();
        
        Mockito.when(ws.addModel("testmodel")).thenReturn(res);
        
        Exception exception = assertThrows(JujuException.class, () -> {
        	jujuController.addModel("testmodel");  
        	});

        String expectedMsg = "testmodel";
        String actualMsg = exception.getMessage();
        assertTrue(actualMsg.contains(expectedMsg));
    }

    
    @Test
    public void test_getModel(){

        ProcessResult res = ProcessResult.builder().exitCode(0).stdout("{\n"
        		+ "    \"models\": [\n"
        		+ "        {\n"
        		+ "            \"name\": \"admin/testmodel\",\n"
        		+ "            \"short-name\": \"testmodel\",\n"
        		+ "            \"model-uuid\": \"2574e2c7-a8b5-4be5-894c-d17c95662561\",\n"
        		+ "            \"model-type\": \"iaas\",\n"
        		+ "            \"controller-uuid\": \"793d364b-8939-47e3-8425-69657005f945\",\n"
        		+ "            \"controller-name\": \"openstack-inari-test-controller\",\n"
        		+ "            \"is-controller\": true,\n"
        		+ "            \"owner\": \"admin\",\n"
        		+ "            \"cloud\": \"openstack-inari-test\",\n"
        		+ "            \"region\": \"RegionOne\",\n"
        		+ "            \"credential\": {\n"
        		+ "                \"name\": \"inari-admin-tenant\",\n"
        		+ "                \"owner\": \"admin\",\n"
        		+ "                \"cloud\": \"openstack-inari-test\"\n"
        		+ "     ").errout("").build();

        Mockito.when(ws.model()).thenReturn(res);
        jujuController.model();
        assertTrue(true);
    }

    @Test
    public void test_delete_Model(){

        ProcessResult res = ProcessResult.builder().exitCode(0).stdout("").errout("Destroying model\n"
        		+ "Waiting for model to be removed.....\n"
        		+ "Model destroyed.\n"
        		+ "").build();

        Mockito.when(ws.removeModel("testmodel")).thenReturn(res);
        jujuController.removeModel("testmodel");
        assertTrue(true);
    }
    
    @Test
    public void test_delete_Model_when_JujuException_throws(){

        ProcessResult res = ProcessResult.builder().exitCode(1).stdout("").errout("ERROR model openstack-inari-test-controller:admin/testmodel not found\n"
        		+ "").build();

        Mockito.when(ws.removeModel("testmodel")).thenReturn(res);
        
        Exception exception = assertThrows(JujuException.class, () -> {
        	jujuController.removeModel("testmodel");  
        	});

        String expectedMsg = "testmodel";
        String actualMsg = exception.getMessage();
        assertTrue(actualMsg.contains(expectedMsg));
    }
    
    @Test
    public void test_deploy_App(){

        ProcessResult res = ProcessResult.builder().exitCode(0).stdout("Executing changes:\n"
        		+ "- upload charm calico from charm-hub for series jammy from channel stable with architecture=amd64\n"
        		+ "- deploy application calico from charm-hub on jammy with stable\n"
        		+ "- set annotations for calico\n"
        		+ "- add unit kubernetes-control-plane/0 to new machine 0\n"
        		+ "- add unit kubernetes-worker/0 to new machine 1\n"
        		+ "").errout("Located bundle \"kubernetes-core\" in charm-hub, revision 1815\n"
        				+ "Located charm \"calico\" in charm-hub, channel stable\n"
        				+ "Located charm \"containerd\" in charm-hub, channel stable\n"
        				+ "added resource kube-apiserver\n"
        				+ "  added resource kube-controller-manager\n"
        				+ "  added resource kube-proxy\n"
        				+ "Deploy of bundle completed.\n"
        				+ "").build();

        Mockito.when(ws.deployApp(Mockito.anyString(),Mockito.anyString())).thenReturn(res);
        jujuController.deployApp("kubernetes-core","ubi-k8s-cluster");
        assertTrue(true);
    }
    
    @Test
    public void test_getApplication(){

        ProcessResult res = ProcessResult.builder().exitCode(0).stdout("calico\":{\"charm\":\"calico\",\"base\":\"ubuntu@22.04\",\"channel\":\"stable\",\"constraints\":{},\"principal\":false,\"exposed\":false,\"remote\":false,\"life\":\"alive\",\"endpoint-bindings\":{\"\":\"alpha\",\"cni\":\"alpha\",\"etcd\":\"alpha\"}}}").errout("").build();
        
        
        Mockito.when(ws.application("calico")).thenReturn(res);
       	jujuController.application("calico");  
        assertTrue(true);
    }

    
    @Test
    public void test_getApplication_when_JujuException_throws(){

        ProcessResult res = ProcessResult.builder().exitCode(1).stdout("").errout("ERROR application \"applicationtest\" not found\n").build();
        
        
        Mockito.when(ws.application("applicationtest")).thenReturn(res);
        
        Exception exception = assertThrows(JujuException.class, () -> {
        	jujuController.application("applicationtest");  
        	});

        String expectedMsg = "applicationtest";
        String actualMsg = exception.getMessage();
        assertTrue(actualMsg.contains(expectedMsg));
    }
    
    @Test
    public void test_delete_Application(){

        ProcessResult res = ProcessResult.builder().exitCode(0).stdout("will remove application kubernetes-control-plane\n"
        		+ "- will remove unit kubernetes-control-plane/0\n").errout("").build();
         
        Mockito.when(ws.removeApplication("kubernetes-control-plane")).thenReturn(res);
        jujuController.removeApplication("kubernetes-control-plane");
        assertTrue(true);
        }

    @Test
    public void test_delete_Application_when_JujuException_throws(){

        ProcessResult res = ProcessResult.builder().exitCode(1).stdout("").errout("\n"
        		+ "ERROR removing application controller failed: application \"applicationtest\" not found\n"
        		+ "").build();
        
        
        Mockito.when(ws.removeApplication("applicationtest")).thenReturn(res);
        
        Exception exception = assertThrows(JujuException.class, () -> {
        	jujuController.removeApplication("applicationtest");  
        	});

        String expectedMsg = "applicationtest";
        String actualMsg = exception.getMessage();
        assertTrue(actualMsg.contains(expectedMsg));
    }

    @Test
    public void test_get_Status(){

        ProcessResult res = ProcessResult.builder().exitCode(0).stdout("{\n"
        		+ "    \"model\": {\n"
        		+ "        \"name\": \"controllertest\",\n"
        		+ "        \"type\": \"iaas\",\n"
        		+ "        \"controller\": \"openstack-inari-test-controller\",\n"
        		+ "        \"cloud\": \"openstack-inari-testcloud\",\n"
        		+ "        \"region\": \"RegionOne\",\n"
        		+ "        \"version\": \"3.2.3\",\n"
        		+ "        \"model-status\": {\n"
        		+ "            \"current\": \"available\",\n"
        		+ "            \"since\": \"15 Oct 2023 03:42:25Z\"\n"
        		+ "        },\n"
        		+ "        \"sla\": \"unsupported\"\n"
        		+ "    },\n"
        		+ "    \"machines\": {},\n"
        		+ "    \"applications\": {},\n"
        		+ "    \"storage\": {},\n"
        		+ "    \"controller\": {\n"
        		+ "        \"timestamp\": \"11:16:30Z\"\n"
        		+ "    }\n"
        		+ "}").errout("").build();

        Mockito.when(ws.status()).thenReturn(res);
        jujuController.status();
        assertTrue(true);
    }

}