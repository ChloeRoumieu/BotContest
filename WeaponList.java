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
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.utils.collections.MyCollections;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
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
    /* generateur de nombre aleatoire */
    private final Random rand;
    /* epsilon delimitant le choix du joueur : prendre la meilleure arme ou la prendre aleatoirement */
    private static final double epsilon = 0.3;
    
    /* Constructeur */
    public WeaponList () {
        weaponsList = new ArrayList<WeaponIA>();
        rand = new Random();
    }
    
        /* retourne les probabilites d'efficacite initiales pour chaque arme, probabilites choisies arbitrairement par les developpeurs */
    public final double probaInitialeWeapon (ItemType typeWeapon) {
       if (typeWeapon == UT2004ItemType.ASSAULT_RIFLE)
           return 0.2;
       if ((typeWeapon == UT2004ItemType.BIO_RIFLE) || (typeWeapon == UT2004ItemType.SNIPER_RIFLE))
           return 0.3;
       if (typeWeapon == UT2004ItemType.LINK_GUN)
           return 0.5;
       if (typeWeapon == UT2004ItemType.SHOCK_RIFLE)
           return 0.4;
       if (typeWeapon == UT2004ItemType.FLAK_CANNON)
           return 0.6;
       if (typeWeapon == UT2004ItemType.ROCKET_LAUNCHER)
           return 0.7;
       if (typeWeapon == UT2004ItemType.TRANSLOCATOR)
           return 0.1;
       if ((typeWeapon == UT2004ItemType.LIGHTNING_GUN) ||(typeWeapon == UT2004ItemType.MINIGUN))
           return 0.35;
       
       return 0.4;
    }
    
    /* retourne les poids initiaux pour chaque arme, poids choisis arbitrairement par les developpeurs */
    public final double weightInitialWeapon (ItemType typeWeapon) {
       if ((typeWeapon == UT2004ItemType.ASSAULT_RIFLE)|| (typeWeapon == UT2004ItemType.BIO_RIFLE))
           return 0.9;
       if (typeWeapon == UT2004ItemType.SNIPER_RIFLE)
           return 0.70;
       if (typeWeapon == UT2004ItemType.LINK_GUN)
           return 0.30;
       if ((typeWeapon == UT2004ItemType.SHOCK_RIFLE)||(typeWeapon == UT2004ItemType.FLAK_CANNON))
           return 0.20;
       if (typeWeapon == UT2004ItemType.ROCKET_LAUNCHER)
           return 0.10;
       if (typeWeapon == UT2004ItemType.TRANSLOCATOR)
           return 0.60;
       if ((typeWeapon == UT2004ItemType.LIGHTNING_GUN) ||(typeWeapon == UT2004ItemType.MINIGUN))
           return 0.50;
       
       return 0.50;
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
        
        double probaInit = probaInitialeWeapon(weapon);
        double weightInit = weightInitialWeapon(weapon);
        WeaponIA newWeaponPrimary = new WeaponIA (weapon, probaInit, weightInit, true);
        WeaponIA newWeaponSecondary = new WeaponIA (weapon, probaInit, weightInit, false);
        
        weaponsList.add(newWeaponPrimary);
        weaponsList.add(newWeaponSecondary);
        Collections.sort(weaponsList);
        return true;
    }
    
    /* mise a jour de l'arme weapon selon l'issue du combat */
    public boolean majWeapon (ItemType weapon, boolean victory) {
        for (WeaponIA w : weaponsList) 
            if (w.getTypeWeapon() == weapon) {
                w.calculNewProba(victory);
                Collections.sort(weaponsList);
                return true;
            }
        return false;
    }
    
    /* transforme une collection de weapon en une liste d'ItemType */
    private List<ItemType> weaponListToItemTypeList (Collection<Weapon> lWeapon) {
        List<ItemType> lItemType = new ArrayList<ItemType>();
        for (Weapon w : lWeapon) {
            lItemType.add(w.getType());
        }
        return lItemType;
    }
    
    /* renvoit la weaponsList sans les armes qui n'appartiennent pas a la liste ownedWeapons */
    public List<WeaponIA> listeOwnedWeaponSorted (List<ItemType> ownedWeapons) {
        List<WeaponIA> listOk = new ArrayList<WeaponIA>();
        for (WeaponIA w : weaponsList) {
            if (ownedWeapons.contains(w.getTypeWeapon()))
                listOk.add(w);
        }
        return listOk;
    }
    
    
    /* simule le choix du joueur pour la prochaine arme qu'il utilisera. ownedWeapon = armes possedees par le joueur */
    public WeaponIA getNextWeapon (Collection<Weapon> ownedWeapons) {
        List<WeaponIA> weaponListSorted = listeOwnedWeaponSorted(weaponListToItemTypeList(ownedWeapons));
        if (Math.random() <= epsilon)
            return getBestWeapon(weaponListSorted);
        else
            return getRandomWeapon(weaponListSorted);
    }
    
    /* retourne l'arme ayant la probabilite d'efficacite la plus elevee */
    public WeaponIA getBestWeapon (List <WeaponIA> weaponListSorted) {
        return weaponListSorted.get(0);
    }
    
    /* retourne une arme de la liste aleatoirement */
      public WeaponIA getRandomWeapon(List <WeaponIA> weaponListSorted) {
        return weaponListSorted.get(rand.nextInt(weaponListSorted.size()));
     }
    
//    TEST A SUPPRIMER
//    public String afficherListe () {
//        String out="Liste !! \n";
//        for (WeaponIA w : weaponsList) {
//            out += w.getTypeWeapon().getName() + " : " + w.getProba();
//            out += " \n";
//        }
//        return out;
//    }
    
    public int getSize() {
        return weaponsList.size();
    }
    
}