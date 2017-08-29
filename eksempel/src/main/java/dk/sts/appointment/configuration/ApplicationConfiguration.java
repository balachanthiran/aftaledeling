package dk.sts.appointment.configuration;

import javax.xml.namespace.QName;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.openehealth.ipf.commons.ihe.ws.WsTransactionConfiguration;
import org.openehealth.ipf.commons.ihe.xds.core.XdsClientFactory;
import org.openehealth.ipf.commons.ihe.xds.iti18.Iti18PortType;
import org.openehealth.ipf.commons.ihe.xds.iti41.Iti41PortType;
import org.openehealth.ipf.commons.ihe.xds.iti43.Iti43PortType;
import org.openehealth.ipf.commons.ihe.xds.iti57.Iti57PortType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import dk.sts.appointment.services.AppointmentXdsRequestBuilderService;
import dk.sts.appointment.services.AppointmentXdsRequestService;
import dk.sts.appointment.utilities.Codes;
import dk.sts.appointment.utilities.OrganisationIdAuthority;
import dk.sts.appointment.utilities.PatientIdAuthority;

public class ApplicationConfiguration {

	private static Logger LOGGER = LoggerFactory.getLogger(ApplicationConfiguration.class);

	@Value("${xds.iti18.endpoint}")
	String xdsIti18Endpoint;

	@Value("${xds.iti18.wsdl}")
	String xdsIti18Wsdl;

	@Value("${xds.iti41.endpoint}")
	String xdsIti41Endpoint;

	@Value("${xds.iti41.wsdl}")
	String xdsIti41Wsdl;

	@Value("${xds.iti43.endpoint}")
	String xdsIti43Endpoint;

	@Value("${xds.iti43.wsdl}")
	String xdsIti43Wsdl;

	@Value("${xds.iti57.endpoint}")
	String xdsIti57Endpoint;

	@Value("${xds.iti57.wsdl}")
	String xdsIti57Wsdl;

	@Bean
	public PatientIdAuthority getPatientIdAuthority() {
		return new PatientIdAuthority(Codes.DK_CPR_OID);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public Iti18PortType getDocumentRegistryServiceIti18() {
		LOGGER.info("Creating Iti18PortType for url: "+xdsIti18Endpoint);

		XdsClientFactory xdsClientFactory = generateXdsRegistryClientFactory(xdsIti18Wsdl, xdsIti18Endpoint, Iti18PortType.class);
		Iti18PortType client = (Iti18PortType) xdsClientFactory.getClient();

		if (LOGGER.isDebugEnabled()) {
			Client proxy = ClientProxy.getClient(client);
			proxy.getOutInterceptors().add(new LoggingOutInterceptor());
			proxy.getInInterceptors().add(new LoggingInInterceptor());
		}

		return client;
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public Iti41PortType getDocumentRepositoryServiceIti41() {
		LOGGER.info("Creating Iti41PortType for url: "+xdsIti41Endpoint);

		XdsClientFactory xdsClientFactory = generateXdsRepositoryClientFactory(xdsIti41Wsdl, xdsIti41Endpoint, Iti41PortType.class);
		Iti41PortType client = (Iti41PortType) xdsClientFactory.getClient();

		if (LOGGER.isDebugEnabled()) {
			Client proxy = ClientProxy.getClient(client);
			proxy.getOutInterceptors().add(new LoggingOutInterceptor());
			proxy.getInInterceptors().add(new LoggingInInterceptor());
		}

		return client;
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public Iti43PortType getDocumentRepositoryServiceIti43() {
		LOGGER.info("Creating Iti43PortType for url: "+xdsIti43Endpoint);

		XdsClientFactory xdsClientFactory = generateXdsRepositoryClientFactory(xdsIti43Wsdl, xdsIti43Endpoint, Iti43PortType.class);
		Iti43PortType client = (Iti43PortType) xdsClientFactory.getClient();
		if (LOGGER.isDebugEnabled()) {
			Client proxy = ClientProxy.getClient(client);
			proxy.getOutInterceptors().add(new LoggingOutInterceptor());
			proxy.getInInterceptors().add(new LoggingInInterceptor());
		}

		return client;
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public Iti57PortType getDocumentRepositoryServiceIti57() {
		LOGGER.info("Creating Iti57PortType for url: "+xdsIti57Endpoint);

		XdsClientFactory xdsClientFactory = generateXdsRegistryClientFactory("urn:ihe:iti:2010", xdsIti57Wsdl, xdsIti57Endpoint, Iti57PortType.class);
		Iti57PortType client = (Iti57PortType) xdsClientFactory.getClient();
		if (LOGGER.isDebugEnabled()) {
			Client proxy = ClientProxy.getClient(client);
			proxy.getOutInterceptors().add(new LoggingOutInterceptor());
			proxy.getInInterceptors().add(new LoggingInInterceptor());
		}

		return client;
	}

	private XdsClientFactory generateXdsRegistryClientFactory(String wsdl, String url, Class<?> clazz){
		return generateXdsRegistryClientFactory("urn:ihe:iti:xds-b:2007", wsdl, url, clazz);
	}

	private XdsClientFactory generateXdsRegistryClientFactory(String namespace, String wsdl, String url, Class<?> clazz){
		final WsTransactionConfiguration WS_CONFIG = new WsTransactionConfiguration(
				new QName(namespace, "DocumentRegistry_Service",
						"ihe"), clazz, new QName(
								namespace,
								"DocumentRegistry_Binding_Soap12", "ihe"), false,
				wsdl, true, false, false, false);

		return new XdsClientFactory(WS_CONFIG, url, null, null,null);
	}

	private XdsClientFactory generateXdsRepositoryClientFactory(String wsdl, String url, Class<?> clazz){
		final WsTransactionConfiguration WS_CONFIG = new WsTransactionConfiguration(
				new QName("urn:ihe:iti:xds-b:2007", "DocumentRepository_Service",
						"ihe"), clazz, new QName(
								"urn:ihe:iti:xds-b:2007",
								"DocumentRepository_Binding_Soap12", "ihe"), true,
				wsdl, true, false, false, false);

		return new XdsClientFactory(WS_CONFIG, url, null, null,null);
	}

	@Bean
	public AppointmentXdsRequestBuilderService xdsRequestBuilderService() {
		AppointmentXdsRequestBuilderService xdsRequestBuilderService = new AppointmentXdsRequestBuilderService();
		return xdsRequestBuilderService;
	}

	@Bean
	public OrganisationIdAuthority organisationIdAuthority() {
		return new OrganisationIdAuthority(Codes.DK_SOR_CLASSIFICAION_OID);
	}

	@Bean
	public AppointmentXdsRequestService xdsRequestService() {
		AppointmentXdsRequestService xdsRequestService = new AppointmentXdsRequestService();
		return xdsRequestService;
	}
}