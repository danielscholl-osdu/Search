package org.opengroup.osdu.search.config;

import com.google.common.base.Strings;
import java.util.regex.PatternSyntaxException;
import lombok.Getter;
import lombok.Setter;
import org.opengroup.osdu.core.common.model.search.DeploymentEnvironment;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties
@Getter
@Setter
public class SearchConfigurationProperties {
	public static final String AUTOCOMPLETE_FEATURE_NAME = "featureFlag.autocomplete.enabled";
	public static final String POLICY_FEATURE_NAME = "featureFlag.policy.enabled";

	//Search query properties
	private Integer queryDefaultLimit = 10;
	private Integer queryLimitMaximum = 1000;
	private Integer aggregationSize = 1000;


	private String elasticDatastoreKind;
	private String elasticDatastoreId;

	//Default Cache Settings
	private Integer schemaCacheExpiration = 60;
	private Integer indexCacheExpiration = 60;
	private Integer elasticCacheExpiration = 1440;
	private Integer cursorCacheExpiration = 60;
	private Integer maximumCacheSize = 20;

	//Kinds Cache expiration 2*24*60
	private Integer kindsCacheExpiration = 2880;

	//Attributes Cache expiration 2*24*60
	private Integer attributesCacheExpiration = 2880;

	private Integer kindsRedisDatabase = 1;
	private Integer cronIndexCleanupThresholdDays = 3;
	private Integer cronEmptyIndexCleanupThresholdDays = 7;

	private String deploymentEnvironment = DeploymentEnvironment.CLOUD.name();
	private String environment;
	private String indexerHost;
	private String searchHost;
	private String storageQueryRecordForConversionHost;
	private String storageQueryRecordHost;
	private String storageRecordsBatchSize;
	private String storageSchemaHost;
	private String entitlementsHost;
	private String indexerQueueHost;
	private String redisSearchHost;
	private String redisSearchPort = "6379";
	private String redisGroupHost;
	private Integer redisGroupPort = 6379;
	private String elasticHost;
	private String elasticPort = "443";
	private String elasticUserPassword;
	private String elasticClusterName;
	private String keyRing;
	private String kmsKey;
	private String cronIndexCleanupPattern;
	private String cronIndexCleanupTenants;
	private String smartSearchCcsDisabled;
	private String securityHttpsCertificateTrust;

	private String gaeService;
	private String gaeVersion;
	private String googleCloudProject;
	private String googleCloudProjectRegion;

	// Max length of exception message that can be logged
	private Integer maxExceptionLogMessageLength = 5000;

	public DeploymentEnvironment getDeploymentEnvironment(){
		return DeploymentEnvironment.valueOf(deploymentEnvironment);
	}

	public String getDeploymentLocation() {
		return googleCloudProjectRegion;
	}

	public String getDeployedServiceId() {
		return gaeService;
	}

	public String getDeployedVersionId() {
		return gaeVersion;
	}


	public boolean isLocalEnvironment() {
		return "local".equalsIgnoreCase(environment);
	}

	public boolean isPreP4d() {
		return isLocalEnvironment() || "evd".equalsIgnoreCase(environment) || "evt".equalsIgnoreCase(environment);
	}

	public boolean isPreDemo() {
		return isPreP4d() || "p4d".equalsIgnoreCase(environment);
	}

	public String[] getIndexCleanupPattern() {
		if (!Strings.isNullOrEmpty(cronIndexCleanupPattern)) {
			try {
				return cronIndexCleanupPattern.split(",");
			} catch (PatternSyntaxException var2) {
			}
		}

		return new String[0];
	}

	public String[] getIndexCleanupTenants() {
		if (!Strings.isNullOrEmpty(cronIndexCleanupTenants)) {
			try {
				return cronIndexCleanupTenants.split(",");
			} catch (PatternSyntaxException var2) {
			}
		}

		return new String[0];
	}

	public final Boolean isSmartSearchCcsDisabled() {
		return Boolean.TRUE.toString().equalsIgnoreCase(smartSearchCcsDisabled);
	}
}
