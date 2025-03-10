/**
 * Copyright 2020 IBM Corp. All Rights Reserved.
 * Copyright 2020 © Amazon Web Services
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.search.provider.aws.di;

import org.opengroup.osdu.core.common.entitlements.EntitlementsAPIConfig;
import org.opengroup.osdu.core.common.entitlements.EntitlementsFactory;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsFactory;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import jakarta.inject.Inject;

@Component
@RequestScope
public class EntitlementsFactoryAws extends AbstractFactoryBean<IEntitlementsFactory> {
	@Value("${AUTHORIZE_API}")
	private String authorizeAPI;

	@Value("${AUTHORIZE_API_KEY:}")
	private String authorizeApiKey;

	@Inject
	private HttpResponseBodyMapper httpResponseBodyMapper;

	@Override
	protected IEntitlementsFactory createInstance() {
		return new EntitlementsFactory(EntitlementsAPIConfig
				.builder()
				.rootUrl(authorizeAPI)
				.apiKey(authorizeApiKey)
				.build(), httpResponseBodyMapper);
	}

	@Override
	public Class<?> getObjectType() {
		return IEntitlementsFactory.class;
	}
}
