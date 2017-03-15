/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.config.configuration.provider;

import org.osgi.framework.BundleContext;
import org.wso2.carbon.secvault.securevault.SecureVault;

import java.util.Optional;

/**
 * Config Provider Data Holder.
 *
 * @since 1.0.0
 */
public class ConfigProviderDataHolder {
    private static ConfigProviderDataHolder instance = new ConfigProviderDataHolder();
    private Optional<SecureVault> optionalSecureVault = Optional.empty();
    private Optional<BundleContext> bundleContext = Optional.empty();

    public static ConfigProviderDataHolder getInstance() {
        return instance;
    }

    private ConfigProviderDataHolder() {
    }

    public Optional<SecureVault> getSecureVault() {
        return optionalSecureVault;
    }

    public void setSecureVault(SecureVault secureVault) {
        this.optionalSecureVault = Optional.ofNullable(secureVault);
    }

    public Optional<BundleContext> getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = Optional.ofNullable(bundleContext);
    }
}
