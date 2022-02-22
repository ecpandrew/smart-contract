/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import com.owlike.genson.annotation.JsonProperty;

@DataType()
public final class Identity {

    @Property()
    private final String context;

    @Property()
    private final String identifier;

    @Property()
    private final String controlledBy;

    @Property()
    private final Map<String, String> publicKeyJwk;

    @Property()
    private final Map<String, String> subjectInfo;

    @Property()
    private final String issuedAt;

    @Property()
    private final String validTo;

    @Property()
    private final String status;

    public String getIdentifier() {
        return identifier;
    }

    public String getContext() {
        return context;
    }

    public String getControlledBy() {
        return controlledBy;
    }

    public Map<String, String> getPublicKeyJwk() {
        return publicKeyJwk;
    }

    public String getIssuedAt() {
        return issuedAt;
    }


    public String getValidTo() {
        return validTo;
    }


    public String getStatus() {
        return status;
    }

    public Map<String, String> getSubjectInfo() {
        return subjectInfo;
    }

    public Identity(@JsonProperty("context") final String context,
                    @JsonProperty("identifier") final String identifier,
                    @JsonProperty("controlledBy") final String controlledBy,
                    @JsonProperty("publicKeyJwk") final Map<String, String> publicKeyJwk,
                    @JsonProperty("subjectInfo") final Map<String, String> subjectInfo,
                    @JsonProperty("status") final String status,
                    @JsonProperty("issuedAt") final String issuedAt,
                    @JsonProperty("validTo") final String validTo) {
        this.context = context;
        this.identifier = identifier;
        this.controlledBy = controlledBy;
        this.publicKeyJwk = publicKeyJwk;
        this.subjectInfo = subjectInfo;
        this.status = status;
        this.issuedAt = issuedAt;
        this.validTo = validTo;
    }





//    public static Identity newECInstance(String context, String id, String controlledBy, String kty, String use, String crv, String x, String y, String kid, String issuedAt, String validTo){
//        HashMap<String, String> publicKey = new HashMap<>();
//        publicKey.put("kty", kty);
//        publicKey.put("kid", kid);
//        publicKey.put("use", use);
//        publicKey.put("crv", crv);
//        publicKey.put("x", x);
//        publicKey.put("y", y);
//        return new Identity(context, id, controlledBy, publicKey,"", issuedAt, validTo);
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identity identity = (Identity) o;
        return context.equals(identity.context) && identifier.equals(identity.identifier) && controlledBy.equals(identity.controlledBy) && publicKeyJwk.equals(identity.publicKeyJwk) && subjectInfo.equals(identity.subjectInfo) && issuedAt.equals(identity.issuedAt) && validTo.equals(identity.validTo) && status.equals(identity.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }


//    @Override
//    public boolean equals(final Object obj) {
//        if (this == obj) {
//            return true;
//        }
//
//        if ((obj == null) || (getClass() != obj.getClass())) {
//            return false;
//        }
//
//        Identity other = (Identity) obj;
//
//        return Objects.deepEquals(
//                new String[] {getContext(),getIdentifier(),getControlledBy()},
//                new String[] {other.getContext(),other.getIdentifier(),other.getControlledBy()});
//
//    }
//
//
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(getIdentifier());
//    }


}
