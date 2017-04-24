/*
 * Copyright (C) 2017 AMIS research group, Faculty of Mathematics and Physics, Charles University in Prague, Czech Republic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mycompany.botcontest;

import cz.cuni.amis.pogamut.base.agent.module.SensomotoricModule;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Combo;
import java.util.logging.Logger;

/**
 *
 * @author Chloé Roumieu
 */
public class OurAdrenalineCombo extends SensomotoricModule<UT2004Bot> {

	private AgentInfo info;

	/**
	 * Tells whether you have adrenaline >= 100, note that using the combo won't change the adrenaline level in the same "logic-cycle".
	 * @return
	 */
	public boolean canPerformCombo() {
		return info.getAdrenaline() >= 100;
	}
    
	/**
	 * Perform "Berserk" combo (bigger damage).
	 * @return whether the combo has been executed
	 */
	public boolean performBerserk() {
		if (canPerformCombo()) {
			act.act(new Combo("xGame.ComboBerserk"));
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Perform "Defensive" combo (every few seconds adds health).
	 * @return whether the combo has been executed
	 */
	public boolean performDefensive() {
		if (canPerformCombo()) {
			act.act(new Combo("xGame.ComboDefensive"));
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Perform "Defensive" combo (bot is invisible and is very hard to spot), note that this combo does not affects PogamutBots ;-(
	 * @return whether the combo has been executed
	 */
	public boolean performInvisible() {
		if (canPerformCombo()) {
			act.act(new Combo("xGame.ComboInvis"));
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Perform "Defensive" combo (bots speed is increased), not advised as it usually breaks running constants, which breaks bot navigation in turn.
	 * @return whether the combo has been executed
	 */
	public boolean performSpeed() {
		if (canPerformCombo()) {
			act.act(new Combo("xGame.ComboSpeed"));
			return true;
		} else {
			return false;
		}
	}
	
    public OurAdrenalineCombo(UT2004Bot bot, AgentInfo info) {
        this(bot, info, null);
    }
    
    public OurAdrenalineCombo(UT2004Bot bot, AgentInfo info, Logger log) {
        super(bot, log);
        this.info = info;
        cleanUp();
    }
    
    @Override
    protected void cleanUp() {
    	super.cleanUp();
    }
    
}