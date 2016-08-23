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

import org.apache.commons.configuration.Configuration;
import org.mobicents.servlet.sip.restcomm.annotations.concurrency.ThreadSafe;
import org.restcomm.sbc.bo.BanList;
import org.restcomm.sbc.bo.BanListList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    22 jul. 2016 18:08:56
 * @class   BanListListConverter.java
 *
 */
@ThreadSafe
public final class BanListListConverter extends AbstractConverter implements JsonSerializer<BanListList> {

    Integer page, pageSize, total;
    String pathUri;

    public BanListListConverter(final Configuration configuration) {
        super(configuration);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(final Class klass) {
        return BanListList.class.equals(klass);
    }

    @Override
    public void marshal(final Object object, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final BanListList list = (BanListList) object;

        writer.startNode("BanLists");
        writer.addAttribute("page", String.valueOf(page));
        writer.addAttribute("numpages", String.valueOf(getTotalPages()));
        writer.addAttribute("pagesize", String.valueOf(pageSize));
        writer.addAttribute("total", String.valueOf(getTotalPages()));
        writer.addAttribute("start", getFirstIndex());
        writer.addAttribute("end", getLastIndex(list));
        writer.addAttribute("uri", pathUri);
        writer.addAttribute("firstpageuri", getFirstPageUri());
        writer.addAttribute("previouspageuri", getPreviousPageUri());
        writer.addAttribute("nextpageuri", getNextPageUri(list));
        writer.addAttribute("lastpageuri", getLastPageUri());

        for (final BanList banList : list.getBanListList()) {
            context.convertAnother(banList);
        }
        writer.endNode();
    }

    // Issue 153: https://bitbucket.org/telestax/telscale-restcomm/issue/153
    // Issue 110: https://bitbucket.org/telestax/telscale-restcomm/issue/110
    @Override
    public JsonObject serialize(BanListList banListList, Type type, JsonSerializationContext context) {

        JsonObject result = new JsonObject();

        JsonArray array = new JsonArray();
        for (BanList banList : banListList.getBanListList()) {
            array.add(context.serialize(banList));
        }

        if (total != null && pageSize != null && page != null) {
            result.addProperty("page", page);
            result.addProperty("num_pages", getTotalPages());
            result.addProperty("page_size", pageSize);
            result.addProperty("total", total);
            result.addProperty("start", getFirstIndex());
            result.addProperty("end", getLastIndex(banListList));
            result.addProperty("uri", pathUri);
            result.addProperty("first_page_uri", getFirstPageUri());
            result.addProperty("previous_page_uri", getPreviousPageUri());
            result.addProperty("next_page_uri", getNextPageUri(banListList));
            result.addProperty("last_page_uri", getLastPageUri());
        }

        result.add("entries", array);

        return result;
    }

    private int getTotalPages() {
        return total / pageSize;
    }

    private String getFirstIndex() {
        return String.valueOf(page * pageSize);
    }

    private String getLastIndex(BanListList list) {
        return String.valueOf((page == getTotalPages()) ? (page * pageSize) + list.getBanListList().size()
                : (pageSize - 1) + (page * pageSize));
    }

    private String getFirstPageUri() {
        return pathUri + "?Page=0&PageSize=" + pageSize;
    }

    private String getPreviousPageUri() {
        return ((page == 0) ? "null" : pathUri + "?Page=" + (page - 1) + "&PageSize=" + pageSize);
    }

    private String getNextPageUri(BanListList list) {
        String lastSid = (page == getTotalPages()) ? "null" : list.getBanListList().get(pageSize - 1).getIpAddress().toString();
        return (page == getTotalPages()) ? "null" : pathUri + "?Page=" + (page + 1) + "&PageSize=" + pageSize + "&AfterSid="
                + lastSid;
    }

    private String getLastPageUri() {
        return pathUri + "?Page=" + getTotalPages() + "&PageSize=" + pageSize;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public void setCount(Integer count) {
        this.total = count;
    }

    public void setPathUri(String pathUri) {
        this.pathUri = pathUri;
    }

}
