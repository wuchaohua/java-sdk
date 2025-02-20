/*
 * Copyright 2014-2020  [fisco-dev]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package org.fisco.bcos.sdk.config.model;

import static org.fisco.bcos.sdk.model.CryptoProviderType.SSM;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import org.fisco.bcos.sdk.config.exceptions.ConfigException;
import org.fisco.bcos.sdk.model.CryptoType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Crypto material configuration, include certs and keys */
public class CryptoMaterialConfig {
    private static Logger logger = LoggerFactory.getLogger(CryptoMaterialConfig.class);
    private String certPath = "conf";
    private String caCertPath;
    private String sdkCertPath;
    private String sdkPrivateKeyPath;
    private String enSSLCertPath;
    private String enSSLPrivateKeyPath;
    private String cryptoProvider = "ssm";
    private String sslKeyIndex;
    private String enSslKeyIndex;

    private InputStream caInputStream;
    private InputStream sdkCertInputStream;
    private InputStream sdkPrivateKeyInputStream;
    private InputStream enSSLCertInputStream;
    private InputStream enSSLPrivateKeyInputStream;

    private int sslCryptoType;

    protected CryptoMaterialConfig() {}

    public CryptoMaterialConfig(ConfigProperty configProperty, int cryptoType)
            throws ConfigException {
        this.sslCryptoType = cryptoType;
        Map<String, Object> cryptoMaterialProperty = configProperty.getCryptoMaterial();
        this.certPath =
                ConfigProperty.getConfigFilePath(
                        ConfigProperty.getValue(cryptoMaterialProperty, "certPath", this.certPath));
        CryptoMaterialConfig defaultCryptoMaterialConfig =
                getDefaultCaCertPath(cryptoType, this.certPath);
        this.caCertPath =
                ConfigProperty.getConfigFilePath(
                        ConfigProperty.getValue(
                                cryptoMaterialProperty,
                                "caCert",
                                defaultCryptoMaterialConfig.getCaCertPath()));
        this.sdkCertPath =
                ConfigProperty.getConfigFilePath(
                        ConfigProperty.getValue(
                                cryptoMaterialProperty,
                                "sslCert",
                                defaultCryptoMaterialConfig.getSdkCertPath()));
        this.sdkPrivateKeyPath =
                ConfigProperty.getConfigFilePath(
                        ConfigProperty.getValue(
                                cryptoMaterialProperty,
                                "sslKey",
                                defaultCryptoMaterialConfig.getSdkPrivateKeyPath()));
        this.enSSLCertPath =
                ConfigProperty.getConfigFilePath(
                        ConfigProperty.getValue(
                                cryptoMaterialProperty,
                                "enSslCert",
                                defaultCryptoMaterialConfig.getEnSSLCertPath()));
        this.enSSLPrivateKeyPath =
                ConfigProperty.getConfigFilePath(
                        ConfigProperty.getValue(
                                cryptoMaterialProperty,
                                "enSslKey",
                                defaultCryptoMaterialConfig.getEnSSLPrivateKeyPath()));
        // load the input stream
        this.caInputStream =
                ConfigProperty.getConfigInputStream(
                        ConfigProperty.getValue(
                                cryptoMaterialProperty,
                                "caCert",
                                defaultCryptoMaterialConfig.getCaCertPath()));
        this.sdkCertInputStream =
                ConfigProperty.getConfigInputStream(
                        ConfigProperty.getValue(
                                cryptoMaterialProperty,
                                "sslCert",
                                defaultCryptoMaterialConfig.getSdkCertPath()));
        this.sdkPrivateKeyInputStream =
                ConfigProperty.getConfigInputStream(
                        ConfigProperty.getValue(
                                cryptoMaterialProperty,
                                "sslKey",
                                defaultCryptoMaterialConfig.getSdkPrivateKeyPath()));
        this.enSSLCertInputStream =
                ConfigProperty.getConfigInputStream(
                        ConfigProperty.getValue(
                                cryptoMaterialProperty,
                                "enSslCert",
                                defaultCryptoMaterialConfig.getEnSSLCertPath()));
        this.enSSLPrivateKeyInputStream =
                ConfigProperty.getConfigInputStream(
                        ConfigProperty.getValue(
                                cryptoMaterialProperty,
                                "enSslKey",
                                defaultCryptoMaterialConfig.getEnSSLPrivateKeyPath()));
        this.cryptoProvider =
                ConfigProperty.getValue(
                        cryptoMaterialProperty,
                        "cryptoProvider",
                        defaultCryptoMaterialConfig.getCryptoProvider());
        this.sslKeyIndex =
                ConfigProperty.getValue(
                        cryptoMaterialProperty,
                        "sslKeyIndex",
                        defaultCryptoMaterialConfig.getSslKeyIndex());
        this.enSslKeyIndex =
                ConfigProperty.getValue(
                        cryptoMaterialProperty,
                        "enSslKeyIndex",
                        defaultCryptoMaterialConfig.getEnSslKeyIndex());

        logger.debug(
                "Load cryptoMaterial, caCertPath: {}, sdkCertPath: {}, sdkPrivateKeyPath:{}, enSSLCertPath: {}, enSSLPrivateKeyPath:{}",
                this.getCaCertPath(),
                this.getSdkCertPath(),
                this.getSdkPrivateKeyPath(),
                this.getEnSSLCertPath(),
                this.getEnSSLPrivateKeyPath());
    }

    public CryptoMaterialConfig getDefaultCaCertPath(int cryptoType, String certPath)
            throws ConfigException {
        CryptoMaterialConfig cryptoMaterialConfig = new CryptoMaterialConfig();
        cryptoMaterialConfig.setCertPath(certPath);
        String smDir = "gm";
        if (cryptoType == CryptoType.ECDSA_TYPE) {
            cryptoMaterialConfig.setCaCertPath(certPath + File.separator + "ca.crt");
            cryptoMaterialConfig.setSdkCertPath(certPath + File.separator + "sdk.crt");
            cryptoMaterialConfig.setSdkPrivateKeyPath(certPath + File.separator + "sdk.key");
            cryptoMaterialConfig.setCryptoProvider(SSM);
        } else if (cryptoType == CryptoType.SM_TYPE) {
            cryptoMaterialConfig.setCaCertPath(
                    certPath + File.separator + smDir + File.separator + "gmca.crt");
            cryptoMaterialConfig.setSdkCertPath(
                    certPath + File.separator + smDir + File.separator + "gmsdk.crt");
            cryptoMaterialConfig.setSdkPrivateKeyPath(
                    certPath + File.separator + smDir + File.separator + "gmsdk.key");
            cryptoMaterialConfig.setEnSSLCertPath(
                    certPath + File.separator + smDir + File.separator + "gmensdk.crt");
            cryptoMaterialConfig.setEnSSLPrivateKeyPath(
                    certPath + File.separator + smDir + File.separator + "gmensdk.key");
            cryptoMaterialConfig.setCryptoProvider(SSM);
        } else {
            throw new ConfigException(
                    "load CryptoMaterialConfig failed, only support ecdsa and sm now, expected 0 or 1, but provided "
                            + cryptoType);
        }
        return cryptoMaterialConfig;
    }

    public String getCertPath() {
        return certPath;
    }

    public void setCertPath(String certPath) {
        this.certPath = certPath;
    }

    public String getCaCertPath() {
        return caCertPath;
    }

    public void setCaCertPath(String caCertPath) {
        this.caCertPath = caCertPath;
    }

    public String getSdkCertPath() {
        return sdkCertPath;
    }

    public void setSdkCertPath(String sdkCertPath) {
        this.sdkCertPath = sdkCertPath;
    }

    public String getSdkPrivateKeyPath() {
        return sdkPrivateKeyPath;
    }

    public void setSdkPrivateKeyPath(String sdkPrivateKeyPath) {
        this.sdkPrivateKeyPath = sdkPrivateKeyPath;
    }

    public String getEnSSLCertPath() {
        return enSSLCertPath;
    }

    public void setEnSSLCertPath(String enSSLCertPath) {
        this.enSSLCertPath = enSSLCertPath;
    }

    public String getEnSSLPrivateKeyPath() {
        return enSSLPrivateKeyPath;
    }

    public void setEnSSLPrivateKeyPath(String enSSLPrivateKeyPath) {
        this.enSSLPrivateKeyPath = enSSLPrivateKeyPath;
    }

    public int getSslCryptoType() {
        return sslCryptoType;
    }

    public void setSslCryptoType(int sslCryptoType) {
        this.sslCryptoType = sslCryptoType;
    }

    public InputStream getCaInputStream() {
        return caInputStream;
    }

    public void setCaInputStream(InputStream caInputStream) {
        this.caInputStream = caInputStream;
    }

    public InputStream getSdkCertInputStream() {
        return sdkCertInputStream;
    }

    public void setSdkCertInputStream(InputStream sdkCertInputStream) {
        this.sdkCertInputStream = sdkCertInputStream;
    }

    public InputStream getSdkPrivateKeyInputStream() {
        return sdkPrivateKeyInputStream;
    }

    public void setSdkPrivateKeyInputStream(InputStream sdkPrivateKeyInputStream) {
        this.sdkPrivateKeyInputStream = sdkPrivateKeyInputStream;
    }

    public InputStream getEnSSLCertInputStream() {
        return enSSLCertInputStream;
    }

    public void setEnSSLCertInputStream(InputStream enSSLCertInputStream) {
        this.enSSLCertInputStream = enSSLCertInputStream;
    }

    public InputStream getEnSSLPrivateKeyInputStream() {
        return enSSLPrivateKeyInputStream;
    }

    public void setEnSSLPrivateKeyInputStream(InputStream enSSLPrivateKeyInputStream) {
        this.enSSLPrivateKeyInputStream = enSSLPrivateKeyInputStream;
    }

    public String getCryptoProvider() {
        return cryptoProvider;
    }

    public void setCryptoProvider(String cryptoProvider) {
        this.cryptoProvider = cryptoProvider;
    }

    public String getSslKeyIndex() {
        return sslKeyIndex;
    }

    public void setSslKeyIndex(String sslKeyIndex) {
        this.sslKeyIndex = sslKeyIndex;
    }

    public String getEnSslKeyIndex() {
        return enSslKeyIndex;
    }

    public void setEnSslKeyIndex(String enSslKeyIndex) {
        this.enSslKeyIndex = enSslKeyIndex;
    }

    @Override
    public String toString() {
        return "CryptoMaterialConfig{"
                + "certPath='"
                + certPath
                + '\''
                + ", caCertPath='"
                + caCertPath
                + '\''
                + ", sdkCertPath='"
                + sdkCertPath
                + '\''
                + ", sdkPrivateKeyPath='"
                + sdkPrivateKeyPath
                + '\''
                + ", enSSLCertPath='"
                + enSSLCertPath
                + '\''
                + ", enSSLPrivateKeyPath='"
                + enSSLPrivateKeyPath
                + '\''
                + ", sslCryptoType="
                + sslCryptoType
                + '}';
    }
}
