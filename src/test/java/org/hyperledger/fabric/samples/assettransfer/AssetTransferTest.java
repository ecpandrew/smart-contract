///*
// * SPDX-License-Identifier: Apache-2.0
// */
//
package org.hyperledger.fabric.samples.assettransfer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.*;

import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

public final class AssetTransferTest {

    private final class MockKeyValue implements KeyValue {

        private final String key;
        private final String value;

        MockKeyValue(final String key, final String value) {
            super();
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getStringValue() {
            return this.value;
        }

        @Override
        public byte[] getValue() {
            return this.value.getBytes();
        }

    }


    private final class MockAssetResultsIterator implements QueryResultsIterator<KeyValue> {

        private final List<KeyValue> assetList;

        MockAssetResultsIterator() {
            super();
            final Genson genson = new Genson();

            String[] args = new String[]{
                    "http://lsdi.ufma.br",
                    "lsdi:ufma:br/entity-manager/",
                    "lsdi:ufma:br/entity-manager/1",
                    "EC",
                    "entity-manager-1",
                    "ES256",
                    "P-256",
                    "LZUHQnp8iCbiKtWKYqOgJlAUG7Ky8oABcNPDLrU49Pg",
                    "V2t-TERnzd2ErA48kOYImzrdmSahAepHrGRETMISiSc",
                    "eyJraWQiOiJlbnRpdHktbWFuYWdlci0xIiwiYWxnIjoiRVMyNTYifQ.bHNkaTp1Zm1hOmJyL2VudGl0eS1tYW5hZ2VyLzE.Wt6n5oqEpukJHbEVWNUCwBThemPcqqRQ7JjQPBcVb1nzy5NSo5OHshhRQMLg0D-oEAeTzZei7eB_LeXCcBpUUg",
                    "nome:LSDi- Entity Manager 1",
                    "descricao:Gerenciador de identidades de dispositivos do LSDi",
                    "coordenador:fssilva",
                    "mantenedor:andrecardoso",
            };

            String applicationContext   = args[0];
            String identityIdentifier   = args[1];
            String controllerIdentifier = args[2];
            String kty                  = args[3];
            String kid                  = args[4];
            String alg                  = args[5];
            String crv                  = args[6];
            String x                    = args[7];
            String y                    = args[8];
            String serializedSignature  = args[9];

            Map<String, String> publicKeyJwk = new HashMap<>();
            Map<String, String> subjectInfo  = new HashMap<>();

            publicKeyJwk.put("kty", kty);
            publicKeyJwk.put("kid", kid);
            publicKeyJwk.put("alg", alg);
            publicKeyJwk.put("crv", crv);
            publicKeyJwk.put("use", "sig");
            publicKeyJwk.put("x", x);
            publicKeyJwk.put("y", y);

            for (int i = 10; i < args.length; i++) { // incremento é antes
                String[] split = args[i].split(":");
                subjectInfo.put(split[0], split[1]);
            }


            Identity identity = new Identity(
                    applicationContext,
                    identityIdentifier+"1",
                    controllerIdentifier,
                    publicKeyJwk,
                    subjectInfo, "active", "dates[0]", "dates[1]");
            Identity identity2 = new Identity(
                    applicationContext,
                    identityIdentifier+"2",
                    controllerIdentifier,
                    publicKeyJwk,
                    subjectInfo, "active", "dates[0]", "dates[1]");
            Identity identity3 = new Identity(
                    applicationContext,
                    identityIdentifier+"3",
                    controllerIdentifier,
                    publicKeyJwk,
                    subjectInfo, "active", "dates[0]", "dates[1]");
            Identity identity4 = new Identity(
                    applicationContext,
                    identityIdentifier+"4",
                    controllerIdentifier,
                    publicKeyJwk,
                    subjectInfo, "active", "dates[0]", "dates[1]");

            String assetJSON = genson.serialize(identity);
            String assetJSON2 = genson.serialize(identity2);
            String assetJSON3 = genson.serialize(identity3);
            String assetJSON4 = genson.serialize(identity4);


            assetList = new ArrayList<KeyValue>();

            assetList.add(new MockKeyValue(identity.getIdentifier(), assetJSON));
            assetList.add(new MockKeyValue(identity2.getIdentifier(), assetJSON2));
            assetList.add(new MockKeyValue(identity3.getIdentifier(), assetJSON3));
            assetList.add(new MockKeyValue(identity4.getIdentifier(), assetJSON4));


        }

        @Override
        public Iterator<KeyValue> iterator() {
            return assetList.iterator();
        }

        @Override
        public void close() throws Exception {
            // do nothing
        }

    }

    //
//    @Test
//    public void invokeUnknownTransaction() {
//        AssetTransfer contract = new AssetTransfer();
//        Context ctx = mock(Context.class);
//
//        Throwable thrown = catchThrowable(() -> {
//            contract.unknownTransaction(ctx);
//        });
//
//        assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
//                .hasMessage("Undefined contract method called");
//        assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo(null);
//
//        verifyZeroInteractions(ctx);
//    }
//
//    @Nested
//    class InvokeReadAssetTransaction {
//
//        @Test
//        public void whenAssetExists() {
//            AssetTransfer contract = new AssetTransfer();
//            Context ctx = mock(Context.class);
//            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//            when(stub.getStringState("asset1"))
//                    .thenReturn("{ \"assetID\": \"asset1\", \"color\": \"blue\", \"size\": 5, \"owner\": \"Tomoko\", \"appraisedValue\": 300 }");
//
//            Asset asset = contract.ReadAsset(ctx, "asset1");
//
//            assertThat(asset).isEqualTo(new Asset("asset1", "blue", 5, "Tomoko", 300));
//        }
//
//        @Test
//        public void whenAssetDoesNotExist() {
//            AssetTransfer contract = new AssetTransfer();
//            Context ctx = mock(Context.class);
//            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//            when(stub.getStringState("asset1")).thenReturn("");
//
//            Throwable thrown = catchThrowable(() -> {
//                contract.ReadAsset(ctx, "asset1");
//            });
//
//            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
//                    .hasMessage("Asset asset1 does not exist");
//            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_NOT_FOUND".getBytes());
//        }
//    }
//
//    @Test
//    void invokeInitLedgerTransaction() {
//        AssetTransfer contract = new AssetTransfer();
//        Context ctx = mock(Context.class);
//        ChaincodeStub stub = mock(ChaincodeStub.class);
//        when(ctx.getStub()).thenReturn(stub);
//
//        contract.InitLedger(ctx);
//
//        InOrder inOrder = inOrder(stub);
//        inOrder.verify(stub).putStringState("asset1", "{\"appraisedValue\":300,\"assetID\":\"asset1\",\"color\":\"blue\",\"owner\":\"Tomoko\",\"size\":5}");
//        inOrder.verify(stub).putStringState("asset2", "{\"appraisedValue\":400,\"assetID\":\"asset2\",\"color\":\"red\",\"owner\":\"Brad\",\"size\":5}");
//        inOrder.verify(stub).putStringState("asset3", "{\"appraisedValue\":500,\"assetID\":\"asset3\",\"color\":\"green\",\"owner\":\"Jin Soo\",\"size\":10}");
//        inOrder.verify(stub).putStringState("asset4", "{\"appraisedValue\":600,\"assetID\":\"asset4\",\"color\":\"yellow\",\"owner\":\"Max\",\"size\":10}");
//        inOrder.verify(stub).putStringState("asset5", "{\"appraisedValue\":700,\"assetID\":\"asset5\",\"color\":\"black\",\"owner\":\"Adrian\",\"size\":15}");
//
//    }
//
    @Nested
    class InvokeCreateAssetTransaction {

        @Test
        public void whenAssetExists() {
//            AssetTransfer contract = new AssetTransfer();

            IdentityContract contract = new IdentityContract();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
//            when(stub.getStringState("asset1"))
//                    .thenReturn("{ \"assetID\": \"asset1\", \"color\": \"blue\", \"size\": 5, \"owner\": \"Tomoko\", \"appraisedValue\": 300 }");

            contract.CreateECIdentity(ctx, "http://lsdi.ufma.br",
                    "lsdi:ufma:br/entity-manager/1",
                    "lsdi:ufma:br/entity-manager/1",
                    "EC",
                    "entity-manager-1",
                    "ES256",
                    "P-256",
                    "LZUHQnp8iCbiKtWKYqOgJlAUG7Ky8oABcNPDLrU49Pg",
                    "V2t-TERnzd2ErA48kOYImzrdmSahAepHrGRETMISiSc",
                    "eyJraWQiOiJlbnRpdHktbWFuYWdlci0xIiwiYWxnIjoiRVMyNTYifQ.bHNkaTp1Zm1hOmJyL2VudGl0eS1tYW5hZ2VyLzE.Wt6n5oqEpukJHbEVWNUCwBThemPcqqRQ7JjQPBcVb1nzy5NSo5OHshhRQMLg0D-oEAeTzZei7eB_LeXCcBpUUg",
                    "nome:LSDi- Entity Manager 1",
                    "descricao:Gerenciador de identidades de dispositivos do LSDi",
                    "coordenador:fssilva",
                    "mantenedor:andrecardoso");


//            System.out.println(thrown.toString());
//            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
//                    .hasMessage("Asset asset1 already exists");
//            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_ALREADY_EXISTS".getBytes());
            }

//        @Test
//        public void whenAssetDoesNotExist() {
//            AssetTransfer contract = new AssetTransfer();
//            Context ctx = mock(Context.class);
//            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//            when(stub.getStringState("asset1")).thenReturn("");
//
//            Asset asset = contract.CreateAsset(ctx, "asset1", "blue", 45, "Siobhán", 60);
//
//            assertThat(asset).isEqualTo(new Asset("asset1", "blue", 45, "Siobhán", 60));
//        }
    }
}
//
//    @Test
//    void invokeGetAllAssetsTransaction() {
//        AssetTransfer contract = new AssetTransfer();
//        Context ctx = mock(Context.class);
//        ChaincodeStub stub = mock(ChaincodeStub.class);
//        when(ctx.getStub()).thenReturn(stub);
//        when(stub.getStateByRange("", "")).thenReturn(new MockAssetResultsIterator());
//
//        String assets = contract.GetAllAssets(ctx);
//
//        assertThat(assets).isEqualTo("[{\"appraisedValue\":300,\"assetID\":\"asset1\",\"color\":\"blue\",\"owner\":\"Tomoko\",\"size\":5},"
//                + "{\"appraisedValue\":400,\"assetID\":\"asset2\",\"color\":\"red\",\"owner\":\"Brad\",\"size\":5},"
//                + "{\"appraisedValue\":500,\"assetID\":\"asset3\",\"color\":\"green\",\"owner\":\"Jin Soo\",\"size\":10},"
//                + "{\"appraisedValue\":600,\"assetID\":\"asset4\",\"color\":\"yellow\",\"owner\":\"Max\",\"size\":10},"
//                + "{\"appraisedValue\":700,\"assetID\":\"asset5\",\"color\":\"black\",\"owner\":\"Adrian\",\"size\":15},"
//                + "{\"appraisedValue\":800,\"assetID\":\"asset6\",\"color\":\"white\",\"owner\":\"Michel\",\"size\":15}]");
//
//    }
//
//    @Nested
//    class TransferAssetTransaction {
//
//        @Test
//        public void whenAssetExists() {
//            AssetTransfer contract = new AssetTransfer();
//            Context ctx = mock(Context.class);
//            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//            when(stub.getStringState("asset1"))
//                    .thenReturn("{ \"assetID\": \"asset1\", \"color\": \"blue\", \"size\": 5, \"owner\": \"Tomoko\", \"appraisedValue\": 300 }");
//
//            Asset asset = contract.TransferAsset(ctx, "asset1", "Dr Evil");
//
//            assertThat(asset).isEqualTo(new Asset("asset1", "blue", 5, "Dr Evil", 300));
//        }
//
//        @Test
//        public void whenAssetDoesNotExist() {
//            AssetTransfer contract = new AssetTransfer();
//            Context ctx = mock(Context.class);
//            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//            when(stub.getStringState("asset1")).thenReturn("");
//
//            Throwable thrown = catchThrowable(() -> {
//                contract.TransferAsset(ctx, "asset1", "Dr Evil");
//            });
//
//            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
//                    .hasMessage("Asset asset1 does not exist");
//            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_NOT_FOUND".getBytes());
//        }
//    }
//
//    @Nested
//    class UpdateAssetTransaction {
//
//        @Test
//        public void whenAssetExists() {
//            AssetTransfer contract = new AssetTransfer();
//            Context ctx = mock(Context.class);
//            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//            when(stub.getStringState("asset1"))
//                    .thenReturn("{ \"assetID\": \"asset1\", \"color\": \"blue\", \"size\": 45, \"owner\": \"Arturo\", \"appraisedValue\": 60 }");
//
//            Asset asset = contract.UpdateAsset(ctx, "asset1", "pink", 45, "Arturo", 600);
//
//            assertThat(asset).isEqualTo(new Asset("asset1", "pink", 45, "Arturo", 600));
//        }
//
//        @Test
//        public void whenAssetDoesNotExist() {
//            AssetTransfer contract = new AssetTransfer();
//            Context ctx = mock(Context.class);
//            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//            when(stub.getStringState("asset1")).thenReturn("");
//
//            Throwable thrown = catchThrowable(() -> {
//                contract.TransferAsset(ctx, "asset1", "Alex");
//            });
//
//            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
//                    .hasMessage("Asset asset1 does not exist");
//            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_NOT_FOUND".getBytes());
//        }
//    }
//
//    @Nested
//    class DeleteAssetTransaction {
//
//        @Test
//        public void whenAssetDoesNotExist() {
//            AssetTransfer contract = new AssetTransfer();
//            Context ctx = mock(Context.class);
//            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//            when(stub.getStringState("asset1")).thenReturn("");
//
//            Throwable thrown = catchThrowable(() -> {
//                contract.DeleteAsset(ctx, "asset1");
//            });
//
//            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
//                    .hasMessage("Asset asset1 does not exist");
//            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_NOT_FOUND".getBytes());
//        }
//    }
//}
