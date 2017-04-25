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

import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weapon;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author Chloé Roumieu
 */
public class WeaponList {
    /* Liste contenant les armes et leurs caracteristiques d'efficacite, triees dans l'ordre decroissant 
    de probabilite d'efficacite */
    private List<WeaponIA> weaponsList;
    
    /* Constructeur */
    public WeaponList () {
        weaponsList = new ArrayList<WeaponIA>();
    }
    
        /* retourne les probabilites d'efficacite initiales pour chaque arme, probabilites choisies arbitrairement par les developpeurs */
    public final float probaInitialeWeapon (ItemType typeWeapon) {
       if (typeWeapon == UT2004ItemType.ASSAULT_RIFLE)
           return (float) 0.2;
       if ((typeWeapon == UT2004ItemType.BIO_RIFLE) || (typeWeapon == UT2004ItemType.SNIPER_RIFLE))
           return (float) 0.3;
       if (typeWeapon == UT2004ItemType.LINK_GUN)
           return (float) 0.5;
       if (typeWeapon == UT2004ItemType.SHOCK_RIFLE)
           return (float) 0.4;
       if (typeWeapon == UT2004ItemType.FLAK_CANNON)
           return (float) 0.6;
       if (typeWeapon == UT2004ItemType.ROCKET_LAUNCHER)
           return (float) 0.7;
       if (typeWeapon == UT2004ItemType.TRANSLOCATOR)
           return (float) 0.1;
       if ((typeWeapon == UT2004ItemType.LIGHTNING_GUN) ||(typeWeapon == UT2004ItemType.MINIGUN))
           return (float) 0.35;
       
       return (float) 0.4;
    }
    
    /* retourne les poids initiaux pour chaque arme, poids choisis arbitrairement par les developpeurs */
    public final int weightInitialWeapon (ItemType typeWeapon) {
       if ((typeWeapon == UT2004ItemType.ASSAULT_RIFLE)|| (typeWeapon == UT2004ItemType.BIO_RIFLE))
           return 8;
       if (typeWeapon == UT2004ItemType.SNIPER_RIFLE)
           return 7;
       if (typeWeapon == UT2004ItemType.LINK_GUN)
           return 3;
       if ((typeWeapon == UT2004ItemType.SHOCK_RIFLE)||(typeWeapon == UT2004ItemType.FLAK_CANNON))
           return 2;
       if (typeWeapon == UT2004ItemType.ROCKET_LAUNCHER)
           return 1;
       if (typeWeapon == UT2004ItemType.TRANSLOCATOR)
           return 6;
       if ((typeWeapon == UT2004ItemType.LIGHTNING_GUN) ||(typeWeapon == UT2004ItemType.MINIGUN))
           return 5;
       
       return 5;
    }
    
    /* retourne vrai si l'arme weapon appartient déjà à la liste, faux sinon */
    private boolean weaponAlreadyIn (ItemType typeWeapon) {
        for (WeaponIA w : weaponsList) 
            if (w.getTypeWeapon() == typeWeapon) 
                return true;
        return false;
    }
    
    /* ajoute une arme à la liste */
    public boolean addWeapon (ItemType weapon) {
        if (weaponAlreadyIn(weapon))
            return false;
        
        float probaInit = probaInitialeWeapon(weapon);
        WeaponIA newWeapon = new WeaponIA (weapon, probaInit, weightInitialWeapon(weapon));
        weaponsList.add(newWeapon);
        Collections.sort(weaponsList);
        return true;
    }
    
    /* mise a jour de l'arme weapon selon l'issue du combat */
    public boolean majWeapon (ItemType weapon, boolean victory) {
        for (WeaponIA w : weaponsList) 
            if (w.getTypeWeapon() == weapon) {
                w.calculNewProba(victory);
               // w.setProba((float)0.9);
                Collections.sort(weaponsList);
                return true;
            }
        return false;
    }
    
    //TEST A SUPPRIMER
    public void afficherListe () {
        for (WeaponIA w : weaponsList) {
            System.out.println(w.getTypeWeapon().getName());
        }
    }
    
    
}