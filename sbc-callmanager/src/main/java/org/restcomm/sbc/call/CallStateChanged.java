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
package org.restcomm.sbc.call;

import org.mobicents.servlet.sip.restcomm.annotations.concurrency.Immutable;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@Immutable
public final class CallStateChanged {
    public static enum State {
        QUEUED("queued"), RINGING("ringing"), CANCELED("canceled"), BUSY("busy"), NOT_FOUND("not-found"), FAILED("failed"), NO_ANSWER(
                "no-answer"), IN_PROGRESS("in-progress"), COMPLETED("completed");

        private final String text;

        private State(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    };

    private final State state;

    public CallStateChanged(final State state) {
        super();
        this.state = state;
    }

    public State state() {
        return state;
    }
}
