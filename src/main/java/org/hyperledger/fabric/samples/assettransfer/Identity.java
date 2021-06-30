/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import java.util.HashMap;
import java.util.Objects;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import com.owlike.genson.annotation.JsonProperty;

@DataType()
public final class Identity {

    @Property()
    private final String context;

    @Property()
    private final String id;

    @Property()
    private final String controlledBy;

    @Property()
    private final HashMap<String, String> publicKeyJwk;


    @Property()
    private final String issuedAt;

    @Property()
    private final String updatedAt;

    @Property()
    private final String validTo;


    public String getId() {
        return id;
    }

    public String getContext() {
        return context;
    }

    public String getControlledBy() {
        return controlledBy;
    }

    public HashMap<String, String> getPublicKeyJwk() {
        return publicKeyJwk;
    }

    public String getIssuedAt() {
        return issuedAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getValidTo() {
        return validTo;
    }



    public Identity(@JsonProperty("context") final String context,
                    @JsonProperty("id") final String id,
                    @JsonProperty("controlledBy") final String controlledBy) {
        this.context = context;
        this.id = id;
        this.controlledBy = controlledBy;
        this.publicKeyJwk = null;
        String[] dates = Utils.getIssueAndExpiracyDate(100);
        this.issuedAt = dates[0];
        this.updatedAt = dates[0];
        this.validTo = dates[1];
    }



    public Identity(@JsonProperty("context") final String context,
                    @JsonProperty("id") final String id,
                    @JsonProperty("controlledBy") final String controlledBy,
                    @JsonProperty("publicKeyJwk") final HashMap<String, String> publicKeyJwk ) {
        this.context = context;
        this.id = id;
        this.controlledBy = controlledBy;
        this.publicKeyJwk = publicKeyJwk;
        String[] dates = Utils.getIssueAndExpiracyDate(1);
        this.issuedAt = dates[0];
        this.updatedAt = dates[0];
        this.validTo = dates[1];
    }

    public static Identity newECInstance(String context, String id, String controlledBy, String kty, String use, String crv, String x, String y, String kid){
        HashMap<String, String> publicKeyJwk = new HashMap<String, String>();
        publicKeyJwk.put("kty", kty);
        publicKeyJwk.put("use", use);
        publicKeyJwk.put("crv", crv);
        publicKeyJwk.put("x", x);
        publicKeyJwk.put("y", y);
        publicKeyJwk.put("kid", kid);
        return new Identity(context, id, controlledBy, publicKeyJwk);
    }








    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        Identity other = (Identity) obj;

        return Objects.deepEquals(
                new String[] {getContext(),getId(),getControlledBy()},
                new String[] {other.getContext(),other.getId(),other.getControlledBy()});

    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Identity{" +
                "context='" + context + '\'' +
                ", id='" + id + '\'' +
                ", controlledBy='" + controlledBy + '\'' +
                ", publicKeyJwk=" + publicKeyJwk +
                ", issuedAt='" + issuedAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", validTo='" + validTo + '\'' +
                '}';
    }
}
