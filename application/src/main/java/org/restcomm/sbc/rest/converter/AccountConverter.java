/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */
package org.restcomm.sbc.rest.converter;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.apache.commons.configuration.Configuration;
import org.mobicents.servlet.sip.restcomm.annotations.concurrency.ThreadSafe;
import org.restcomm.sbc.bo.Account;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@ThreadSafe
public final class AccountConverter extends AbstractConverter implements JsonSerializer<Account> {
   // private final String apiVersion;
   // private final String rootUri;

    public AccountConverter(final Configuration configuration) {
        super(configuration);
     //   this.apiVersion = configuration.getString("api-version");
     //   rootUri = StringUtils.addSuffixIfNotPresent(configuration.getString("root-uri"), "/");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(final Class klass) {
        return Account.class.equals(klass);
    }

    @Override
    public void marshal(final Object object, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final Account account = (Account) object;
        writer.startNode("Account");
        writeSid(account.getSid(), writer);
        writeFriendlyName(account.getFriendlyName(), writer);
        writeEmailAddress(account, writer);
        writeStatus(account.getStatus().toString(), writer);
        writeRoleInfo(account.getRole(), writer);
        writeType(account.getType().toString(), writer);
        writeDateCreated(account.getDateCreated(), writer);
        writeDateUpdated(account.getDateUpdated(), writer);
        writeAuthToken(account, writer);
        writeUri(account.getUri(), writer);
        writer.endNode();
    }

    @Override
    public JsonElement serialize(final Account account, final Type type, final JsonSerializationContext context) {
        final JsonObject object = new JsonObject();
        writeSid(account.getSid(), object);
        writeFriendlyName(account.getFriendlyName(), object);
        writeEmailAddress(account, object);
        writeType(account.getType().toString(), object);
        writeStatus(account.getStatus().toString(), object);
        writeRoleInfo(account.getRole(), object);
        writeDateCreated(account.getDateCreated(), object);
        writeDateUpdated(account.getDateUpdated(), object);
        writeAuthToken(account, object);
        writeUri(account.getUri(), object);
        return object;
    }

    private void writeAuthToken(final Account account, final HierarchicalStreamWriter writer) {
        writer.startNode("AuthToken");
        writer.setValue(account.getAuthToken());
        writer.endNode();
    }

    private void writeAuthToken(final Account account, final JsonObject object) {
        object.addProperty("auth_token", account.getAuthToken());
    }

    

    private void writeEmailAddress(final Account account, final HierarchicalStreamWriter writer) {
        writer.startNode("EmailAddress");
        writer.setValue(account.getEmailAddress());
        writer.endNode();
        writer.close();
    }

    private void writeEmailAddress(final Account account, final JsonObject object) {
        object.addProperty("email_address", account.getEmailAddress());
    }

    private void writeRoleInfo(final String role, final HierarchicalStreamWriter writer) {
        writer.startNode("Role");
        writer.setValue(role);
        writer.endNode();
        writer.close();
    }

    private void writeRoleInfo(final String role, final JsonObject object) {
        object.addProperty("role", role);
    }

}
