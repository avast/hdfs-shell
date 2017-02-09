/*
 * Copyright 2011-2012 the original author or authors.
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
package com.avast.server.hdfsshell.ui;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.support.DefaultBannerProvider;
import org.springframework.stereotype.Component;

/**
 * @author Jarred Li
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ShellBannerProvider extends DefaultBannerProvider {

    public String getBanner() {
        return "";//we are using Spring Boot for that
    }


    public String getVersion() {
        return ShellBannerProvider.versionInfo();
    }

	public String getWelcomeMessage() {
		return "Welcome to HDFS-shell CLI";
	}
	
	@Override
	public String getProviderName() {
		return "HDFS-shell CLI";
	}

    public static String versionInfo() {
        Package pkg = ShellBannerProvider.class.getPackage();
        String version = null;
        if (pkg != null) {
            version = pkg.getImplementationVersion();
        }
        return (version != null ? version : "Unknown Version");
    }

}