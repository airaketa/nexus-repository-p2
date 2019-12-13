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
package org.sonatype.nexus.repository.p2.internal.api;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Path;

import org.sonatype.nexus.repository.rest.api.AbstractRepositoriesApiResource;
import org.sonatype.nexus.repository.rest.api.AbstractRepositoryApiRequestToConfigurationConverter;
import org.sonatype.nexus.repository.rest.api.AuthorizingRepositoryManager;
import org.sonatype.nexus.repository.rest.api.RepositoriesApiResource;
import org.sonatype.nexus.rest.Resource;

@Named
@Singleton
@Path(RepositoriesApiResource.RESOURCE_URI + "/p2/proxy")
public class P2ProxyRepositoriesApiResource
    extends AbstractRepositoriesApiResource<P2ProxyRepositoryApiRequest>
    implements Resource, P2ProxyRepositoriesApiResourceDoc
{
  @Inject
  public P2ProxyRepositoriesApiResource(final AuthorizingRepositoryManager authorizingRepositoryManager,
                                        final AbstractRepositoryApiRequestToConfigurationConverter<P2ProxyRepositoryApiRequest> configurationConverter)
  {
    super(authorizingRepositoryManager, configurationConverter);
  }
}
