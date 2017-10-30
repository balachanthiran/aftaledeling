package dk.sds.appointment.configuration;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.openehealth.ipf.commons.ihe.xds.iti18.Iti18PortType;
import org.openehealth.ipf.commons.ihe.xds.iti41.Iti41PortType;
import org.openehealth.ipf.commons.ihe.xds.iti43.Iti43PortType;
import org.openehealth.ipf.commons.ihe.xds.iti57.Iti57PortType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.client.RestTemplate;

import dk.sds.appointment.dgws.DgwsSoapDecorator;
import dk.sds.appointment.dgws.STSRequestHelper;
import dk.sosi.seal.SOSIFactory;
import dk.sosi.seal.pki.SOSITestFederation;
import dk.sosi.seal.vault.CredentialVault;
import dk.sosi.seal.vault.CredentialVaultException;
import dk.sosi.seal.vault.FileBasedCredentialVault;

@PropertySource("classpath:dgws.properties")
public class DgwsConfiguration {

	@Value("${keystore.alias}")
	private String keystoreAlias;

	@Value("${keystore.filename}")
	private String keystoreFilename;

	@Value("${keystore.password}")
	private String keystorePassword;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private ApplicationContext appContext;

	@PostConstruct 
	public void init() {
		// The SOSI Seal Library reads the alias value using System.getProperty()
		System.setProperty("dk.sosi.seal.vault.CredentialVault#Alias", keystoreAlias);
		DgwsSoapDecorator dgwsSoapDecorator = appContext.getBean(DgwsSoapDecorator.class);

		// Add DGWS decorator to all ITI beans
		Iti41PortType iti41 = appContext.getBean(Iti41PortType.class);
		Client proxy41 = ClientProxy.getClient(iti41);
		proxy41.getOutInterceptors().add(dgwsSoapDecorator);
		
		Iti43PortType iti43 = appContext.getBean(Iti43PortType.class);
		Client proxy43 = ClientProxy.getClient(iti43);
		proxy43.getOutInterceptors().add(dgwsSoapDecorator);

		Iti18PortType iti18 = appContext.getBean(Iti18PortType.class);
		Client proxy18 = ClientProxy.getClient(iti18);
		proxy18.getOutInterceptors().add(dgwsSoapDecorator);

		Iti57PortType iti57 = appContext.getBean(Iti57PortType.class);
		Client proxy57 = ClientProxy.getClient(iti57);
		proxy57.getOutInterceptors().add(dgwsSoapDecorator);
	}

	@Bean
	public DgwsSoapDecorator dgwsSoapDecorator() {
		return new DgwsSoapDecorator();
	}

	@Bean
	public CredentialVault getVault() throws CredentialVaultException, IOException {
		// Design choice: The keystore should be external to the application, and the following code
		//                requires that the keystore resides in a config folder relative to where
		//                the application is started
		Resource resource = resourceLoader.getResource("classpath:" + keystoreFilename);

		return new FileBasedCredentialVault(new Properties(), resource.getFile(), keystorePassword);
	}

	@Bean
	public SOSIFactory createSOSIFactory() throws CertificateException, IOException {
		Properties props = new Properties(System.getProperties());
		props.setProperty(SOSIFactory.PROPERTYNAME_SOSI_VALIDATE, Boolean.toString(true));

		return new SOSIFactory(new SOSITestFederation(new Properties()), getVault(), props);
	}


	@Bean
	public STSRequestHelper stsRequestHelper() {
		STSRequestHelper requestHelper = new STSRequestHelper();
		return requestHelper;
	}

	@Bean(name="dgwsTemplate")
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}