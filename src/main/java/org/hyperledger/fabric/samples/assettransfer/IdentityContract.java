/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import java.util.ArrayList;
import java.util.List;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.Genson;

@Contract(
        name = "identity",
        info = @Info(
                title = "Identity Manager Contect",
                description = "The LSDi Identity Management Contract",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "andre.cardoso@lsdi.ufma.br",
                        name = "André Cardoso",
                        url = "http://www.lsdi.ufma.br/")))
@Default
public final class IdentityContract implements ContractInterface {

    private final Genson genson = new Genson();

    private enum IdentityErrors {
        IDENTITY_NOT_FOUND,
        IDENTITY_ALREADY_EXISTS
    }

    /**
     * Create one initial identity on the ledger.
     *
     * @param ctx the transaction context
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void InitLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        CreateIdentity(ctx, "http://www.lsdi.ufma.br/" ,"lsdi:identity:first", "lsdi:identity:first");

    }

    /**
     * Creates a new identity on the ledger.
     *
     * @param ctx the transaction context
     * @return the created asset
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Identity CreateIdentity(final Context ctx, final String context, final String id, final String controlledBy) {
        ChaincodeStub stub = ctx.getStub();

        if (IdentityExists(ctx, id)) {
            String errorMessage = String.format("Identity %s already exists", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, IdentityErrors.IDENTITY_ALREADY_EXISTS.toString());
        }
        Identity identity = new Identity(context, id, controlledBy);
        String assetJSON = genson.serialize(identity);
        stub.putStringState(id, assetJSON);
        return identity;
    }

    /**
     * Creates a new identity on the ledger. This method expects only ECKeys as key;
     *
     * @param ctx the transaction context
     * @return the created asset
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Identity CreateECIdentity(final Context ctx, final String context, final String id, final String controlledBy) {
        ChaincodeStub stub = ctx.getStub();

        if (IdentityExists(ctx, id)) {
            String errorMessage = String.format("Identity %s already exists", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, IdentityErrors.IDENTITY_ALREADY_EXISTS.toString());
        }
        Identity identity = new Identity(context, id, controlledBy);
        String assetJSON = genson.serialize(identity);
        stub.putStringState(id, assetJSON);
        return identity;
    }




    /**
     * Retrieves an asset with the specified ID from the ledger.
     *
     * @param ctx the transaction context
     * @param id the ID of the identity
     * @return the asset found on the ledger if there was one
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Identity ReadIdentity(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String identityJSON = stub.getStringState(id);

        if (identityJSON == null || identityJSON.isEmpty()) {
            String errorMessage = String.format("Identity %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, IdentityErrors.IDENTITY_NOT_FOUND.toString());
        }

        Identity identity = genson.deserialize(identityJSON, Identity.class);
        return identity;
    }

    /**
     * Checks the existence of the identity on the ledger
     *
     * @param ctx the transaction context
     * @param id the ID of the identity
     * @return boolean indicating the existence of the asset
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean IdentityExists(final Context ctx, final String id) {
        ChaincodeStub stub = ctx.getStub();
        String identityJSON = stub.getStringState(id);

        return (identityJSON != null && !identityJSON.isEmpty());
    }



    /**
     * Retrieves all assets from the ledger.
     *
     * @param ctx the transaction context
     * @return array of assets found on the ledger
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllIdentities(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<Identity> queryResults = new ArrayList<Identity>();

        // To retrieve all assets from the ledger use getStateByRange with empty startKey & endKey.
        // Giving empty startKey & endKey is interpreted as all the keys from beginning to end.
        // As another example, if you use startKey = 'asset0', endKey = 'asset9' ,
        // then getStateByRange will retrieve asset with keys between asset0 (inclusive) and asset9 (exclusive) in lexical order.
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result: results) {
            Identity identity = genson.deserialize(result.getStringValue(), Identity.class);
            queryResults.add(identity);
            System.out.println(identity.toString());
        }

        final String response = genson.serialize(queryResults);

        return response;
    }
}