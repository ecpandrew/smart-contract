/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;


import java.security.interfaces.ECPublicKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.util.Base64URL;
import netscape.javascript.JSObject;
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
        IDENTITY_ALREADY_EXISTS,
        INVALID_SIGNATURE

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
    public Identity CreateIdentity(final Context ctx,
                                   final String context,
                                   final String id,
                                   final String controlledBy) {
        ChaincodeStub stub = ctx.getStub();

        if (IdentityExists(ctx, id)) {
            String errorMessage = String.format("Identity %s already exists", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, IdentityErrors.IDENTITY_ALREADY_EXISTS.toString());
        }

        Identity identity = new Identity(context, id, controlledBy, null , null ,"active", null , null);

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
    public Identity CreateECIdentity(
            final Context ctx,
            final String... args
) {
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
        String[] dates = Utils.getIssueAndExpiracyDate(1);
        ChaincodeStub stub = ctx.getStub();

        if (IdentityExists(ctx, identityIdentifier)) {
            String errorMessage = String.format("Identity %s already exists", identityIdentifier);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, IdentityErrors.IDENTITY_ALREADY_EXISTS.toString());
        }
        publicKeyJwk.put("kty", kty);
        publicKeyJwk.put("kid", kid);
        publicKeyJwk.put("alg", alg);
        publicKeyJwk.put("crv", crv);
        publicKeyJwk.put("use", "sig");
        publicKeyJwk.put("x", x);
        publicKeyJwk.put("y", y);

        for (int i = 9; i < args.length; i++) { // incremento é antes
            String[] split = args[i].split(":");
            subjectInfo.put(split[0], split[1]);
        }

        boolean isRequestValid = false;

        if(identityIdentifier.equals(controllerIdentifier)){
            isRequestValid = true;
        }else{
            Identity controller = ReadIdentity(ctx, controllerIdentifier);
            try {
                isRequestValid = validateSignature(controller, serializedSignature);
            } catch (ParseException | JOSEException e) {
                e.printStackTrace();
                String errorMessage = String.format("Error parsing signature from %s", controllerIdentifier);
                throw new ChaincodeException(errorMessage, IdentityErrors.INVALID_SIGNATURE.toString());
            }
        }

        if (isRequestValid){
            // accept new identity proposition
            Identity identity = new Identity(
                    applicationContext,
                    identityIdentifier,
                    controllerIdentifier, publicKeyJwk, subjectInfo, "active", dates[0], dates[1]);

            String assetJSON = genson.serialize(identity);
            stub.putStringState(identityIdentifier, assetJSON);
            return identity;
        }else{
            String errorMessage = String.format("Signature from %s not valid!", controllerIdentifier);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, IdentityErrors.INVALID_SIGNATURE.toString());
        }

    }

    public boolean validateSignature(Identity controller, String serializedSignature) throws ParseException, JOSEException {
        Map<String, String> controllerPublicKeyJwt = controller.getPublicKeyJwk();
        Base64URL x = Base64URL.from(controllerPublicKeyJwt.get("x"));
        Base64URL y = Base64URL.from(controllerPublicKeyJwt.get("y"));
        ECKey controllerECKey = new ECKey(
                Curve.parse(controllerPublicKeyJwt.get("crv")),
                x,
                y,
                KeyUse.SIGNATURE,
                null,
                Algorithm.parse(controllerPublicKeyJwt.get("alg")),
                controllerPublicKeyJwt.get("kid"),
                null,null,null,null,null);
        JWSObject jwsObject = JWSObject.parse(serializedSignature);
        JWSVerifier verifier = new ECDSAVerifier(controllerECKey.toECPublicKey());
        return verifier.verify(jwsObject.getHeader(), jwsObject.getSigningInput(), jwsObject.getSignature());
    }



    /**
     * Creates a new identity on the ledger. This method expects only RSAKeys as key;
     *
     * @param ctx the transaction context
     * @return the created asset
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Identity CreateRSAIdentity(
            final Context ctx,
            final String... args
    ) {

        ChaincodeStub stub = ctx.getStub();

        if (IdentityExists(ctx, args[1])) {
            String errorMessage = String.format("Identity %s already exists", args[1]);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, IdentityErrors.IDENTITY_ALREADY_EXISTS.toString());
        }

        HashMap<String, String> publicKeyJwk = new HashMap<>();
        HashMap<String, String> subjectInfo = new HashMap<>();
        String[] dates = Utils.getIssueAndExpiracyDate(1);

        publicKeyJwk.put("kty", args[3]);
        publicKeyJwk.put("kid", args[4]);
        publicKeyJwk.put("e", args[5]);
        publicKeyJwk.put("use", "sig");
        publicKeyJwk.put("alg", args[6]);
        publicKeyJwk.put("n", args[7]);

        for (int i = 8; i < args.length; i++) {
            String[] split = args[i].split(":");
            subjectInfo.put(split[0], split[1]);
        }

        //todo(): fazer um verificação da assinatura


        Identity identity = new Identity(args[0], args[1], args[2], publicKeyJwk, subjectInfo, "active", dates[0], dates[1]);

        String assetJSON = genson.serialize(identity);

        stub.putStringState(args[1], assetJSON);
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
        }

        final String response = genson.serialize(queryResults);

        return response;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetIdentitiesByController(final Context ctx, String controllerId) {
        ChaincodeStub stub = ctx.getStub();

        List<Identity> queryResults = new ArrayList<Identity>();


        QueryResultsIterator<KeyValue> results = stub.getQueryResult("" +
                "" +
                "  SELECT org.hyperledger.fabric.samples.assettransfer.Identity" +
                "  WHERE (controlledBy = "+ controllerId + " )  " +
                "  ORDER BY [issuedAt] ");

        for (KeyValue result: results) {
            Identity identity = genson.deserialize(result.getStringValue(), Identity.class);
            queryResults.add(identity);
        }

        return genson.serialize(queryResults);
    }

}
