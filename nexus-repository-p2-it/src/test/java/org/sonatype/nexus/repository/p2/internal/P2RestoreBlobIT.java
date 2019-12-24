package org.sonatype.nexus.repository.p2.internal;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.sonatype.goodies.httpfixture.server.fluent.Behaviours;
import org.sonatype.goodies.httpfixture.server.fluent.Server;
import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobStoreManager;
import org.sonatype.nexus.blobstore.restore.RestoreBlobStrategy;
import org.sonatype.nexus.common.app.BaseUrlHolder;
import org.sonatype.nexus.pax.exam.NexusPaxExamSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.AssetEntityAdapter;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.testsuite.testsupport.NexusITSupport;
import org.sonatype.nexus.testsuite.testsupport.blobstore.restore.BlobstoreRestoreTestHelper;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.sonatype.nexus.blobstore.api.BlobAttributesConstants.HEADER_PREFIX;
import static org.sonatype.nexus.blobstore.api.BlobStore.BLOB_NAME_HEADER;
import static org.sonatype.nexus.blobstore.api.BlobStore.CONTENT_TYPE_HEADER;
import static org.sonatype.nexus.repository.storage.Bucket.REPO_NAME_HEADER;

public class P2RestoreBlobIT
    extends P2ITSupport
{
  @Inject
  private BlobstoreRestoreTestHelper testHelper;

  @Inject
  @Named("p2")
  private RestoreBlobStrategy p2RestoreBlobStrategy;

  private static final String PROXY_REPO_NAME = "p2-proxy";

  private Server proxyServer;

  private P2Client proxyClient;

  private Repository proxyRepository;

  @Configuration
  public static Option[] configureNexus() {
    return NexusPaxExamSupport.options(
        NexusITSupport.configureNexusBase(),
        nexusFeature("org.sonatype.nexus.plugins", "nexus-repository-p2"),
        nexusFeature("org.sonatype.nexus.plugins", "nexus-restore-p2")
    );
  }

  @Before
  public void setup() throws Exception {
    BaseUrlHolder.set(this.nexusUrl.toString());

    proxyServer = Server.withPort(0)
        .serve("/" + VALID_PACKAGE_URL)
        .withBehaviours(Behaviours.file(testData.resolveFile(PACKAGE_NAME)))
        .start();

    proxyRepository = repos.createP2Proxy(PROXY_REPO_NAME, "http://localhost:" + proxyServer.getPort() + "/");
    proxyClient = p2Client(proxyRepository);

    assertThat(proxyClient.get(VALID_PACKAGE_URL).getStatusLine().getStatusCode(), is(HttpStatus.OK));
  }

  @After
  public void tearDown() throws Exception {
    if (proxyServer != null) {
      proxyServer.stop();
    }
  }

  @Test
  public void testMetadataRestoreWhenBothAssetsAndComponentsAreMissing() throws Exception {
    verifyMetadataRestored(testHelper::simulateComponentAndAssetMetadataLoss);
  }

  @Test
  public void testMetadataRestoreWhenOnlyAssetsAreMissing() throws Exception {
    verifyMetadataRestored(testHelper::simulateAssetMetadataLoss);
  }

  @Test
  public void testMetadataRestoreWhenOnlyComponentsAreMissing() throws Exception {
    verifyMetadataRestored(testHelper::simulateComponentMetadataLoss);
  }

  @Test
  public void testNotDryRunRestore()
  {
    runBlobRestore(false);
    testHelper.assertAssetInRepository(proxyRepository, VALID_PACKAGE_URL);
  }

  @Test
  public void testDryRunRestore()
  {
    runBlobRestore(true);
    testHelper.assertAssetNotInRepository(proxyRepository, VALID_PACKAGE_URL);
  }

  private void runBlobRestore(final boolean isDryRun) {
    Asset asset;
    Blob blob;
    try (StorageTx tx = getStorageTx(proxyRepository)) {
      tx.begin();
      asset = tx.findAssetWithProperty(AssetEntityAdapter.P_NAME, VALID_PACKAGE_URL,
          tx.findBucket(proxyRepository));
      assertThat(asset, Matchers.notNullValue());
      blob = tx.getBlob(asset.blobRef());
    }
    testHelper.simulateAssetMetadataLoss();
    Properties properties = new Properties();
    properties.setProperty(HEADER_PREFIX + REPO_NAME_HEADER, proxyRepository.getName());
    properties.setProperty(HEADER_PREFIX + BLOB_NAME_HEADER, asset.name());
    properties.setProperty(HEADER_PREFIX + CONTENT_TYPE_HEADER, asset.contentType());

    p2RestoreBlobStrategy.restore(properties, blob, BlobStoreManager.DEFAULT_BLOBSTORE_NAME, isDryRun);
  }

  private void verifyMetadataRestored(final Runnable metadataLossSimulation) throws Exception {
    metadataLossSimulation.run();

    testHelper.runRestoreMetadataTask();

    testHelper.assertComponentInRepository(proxyRepository, PACKAGE_NAME);

    testHelper.assertAssetMatchesBlob(proxyRepository, VALID_PACKAGE_URL);

    testHelper.assertAssetAssociatedWithComponent(proxyRepository, PACKAGE_NAME, VALID_PACKAGE_URL);

    assertThat(proxyClient.get(VALID_PACKAGE_URL).getStatusLine().getStatusCode(), is(HttpStatus.OK));
  }
}
