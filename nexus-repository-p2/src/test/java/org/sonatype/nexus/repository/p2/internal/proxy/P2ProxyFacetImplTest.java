/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2017-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.p2.internal.proxy;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.repository.p2.internal.metadata.ArtifactsXmlAbsoluteUrlRemover;
import org.sonatype.nexus.repository.p2.internal.metadata.P2Attributes;
import org.sonatype.nexus.repository.p2.internal.util.AttributesParserFeatureXml;
import org.sonatype.nexus.repository.p2.internal.util.AttributesParserManifest;
import org.sonatype.nexus.repository.p2.internal.util.P2DataAccess;
import org.sonatype.nexus.repository.p2.internal.util.TempBlobConverter;
import org.sonatype.nexus.repository.storage.TempBlob;

import java.util.Optional;

import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class P2ProxyFacetImplTest
    extends TestSupport
{
  private static final String EXTENSION = "jar";
  private static final String FAKE_VERSION = "1.2.3-56";
  private static final String JAR_NAME = "org.eclipse.core.runtime.feature_1.2.100.v20170912-1859.jar";

  @Mock
  private P2DataAccess p2DataAccess;

  @Mock
  private ArtifactsXmlAbsoluteUrlRemover artifactsXmlAbsoluteUrlRemover;

  @Mock
  private TempBlobConverter tempBlobConverter;

  @Spy @InjectMocks
  private AttributesParserFeatureXml xmlParser;

  @Spy @InjectMocks
  private AttributesParserManifest jarParser;

  @Mock
  private TempBlob tempBlob;

  private P2ProxyFacetImpl underTest;

  @Before
  public void setUp() throws Exception {
    underTest = new P2ProxyFacetImpl(p2DataAccess, artifactsXmlAbsoluteUrlRemover, xmlParser, jarParser);
  }

  @Test
  public void getVersion() throws Exception {
    when(tempBlob.get()).thenReturn(getClass().getResourceAsStream(JAR_NAME));
    doReturn(of(buildWithVersionAndExtension())).when(jarParser).getAttributesFromBlob(any(), any());

    P2Attributes p2Attributes = underTest
        .mergeAttributesFromTempBlob(tempBlob, buildWithVersionAndExtension());

    assertThat(p2Attributes.getComponentVersion(), is(equalTo(FAKE_VERSION)));
  }

  @Test
  public void getUnknownVersion() throws Exception {
    doReturn(Optional.empty()).when(jarParser).getAttributesFromBlob(any(), anyString());
    when(tempBlob.get()).thenReturn(getClass().getResourceAsStream(JAR_NAME));

    P2Attributes p2Attributes = buildWithVersionAndExtension();
    Assert.assertEquals(p2Attributes, underTest.mergeAttributesFromTempBlob(tempBlob, p2Attributes));;
  }

  @Test
  public void getJarWithJarFile() throws Exception {
    when(tempBlob.get()).thenAnswer((a) -> getClass().getResourceAsStream(JAR_NAME));
    assertThat(xmlParser.getAttributesFromBlob(tempBlob, EXTENSION).isPresent(), is(true));
  }

  @Test
  public void getJarWithPackGz() throws Exception {
    when(tempBlobConverter.getJarFromPackGz(tempBlob)).thenReturn(getClass().getResourceAsStream(JAR_NAME));
    when(tempBlob.get()).thenAnswer((a) -> getClass().getResourceAsStream(JAR_NAME));

    assertThat(xmlParser.getAttributesFromBlob(tempBlob, EXTENSION).isPresent(), is(true));
  }

  private P2Attributes buildWithVersionAndExtension() {
    return P2Attributes.builder()
        .componentVersion(FAKE_VERSION)
        .extension(EXTENSION).build();
  }
}
