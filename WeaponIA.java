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


import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
/**
 *
 * @author Chloé Roumieu
 */
public class WeaponIA implements Comparable<WeaponIA> {
    /* Type de l'arme concernee */
    private final ItemType weapon;
    /* probabilite d'efficacite de l'arme dans un combat */
    private double probaEfficiency;
    /* poids de l'arme */
    private double weight;
    /* nombre de combats gagnés avec l'arme */
    private int nbVictory;
    /*nombre de combats perdus avec l'arme */
    private int nbDefeat;
    /* Tir primaire ou secondaire */
    private final boolean primaryShoot;
    
    /* Constructeur */
    public WeaponIA (ItemType weapon, double probaEfficiency, double weight, boolean primaryShoot) {
        this.weapon = weapon;
        this.probaEfficiency = probaEfficiency;
        this.weight = weight;
        this.nbVictory = 0;
        this.nbDefeat = 0;
        this.primaryShoot = primaryShoot;
    }
    
    public boolean getPrimary () {
        return primaryShoot;
    }
    
    public double getProba () {
        return this.probaEfficiency;
    }
    
    public double getWeight () {
        return this.weight;
    }
    
    public void setProba (double nProba) {
        this.probaEfficiency = nProba;
    }
    
    public void setWeight (int nWeight) {
        this.weight = nWeight;
    }
    
    /* retourne le type de l'arme */
    public ItemType getTypeWeapon () {
        return weapon;
    }
    
    /* calcul de la nouvelle probabilite d'une arme apres la fin d'un combat */
    public void calculNewProba (boolean combatGagne) {
        if (combatGagne) nbVictory++;
        else nbDefeat++;
        this.probaEfficiency = (probaEfficiency * weight + nbVictory) / (weight + nbVictory + nbDefeat);
    }

    @Override
    public int compareTo(WeaponIA toComp) {
        int diff = (int) ((int) (toComp.getProba()*100) - (this.probaEfficiency*100));
        if (diff == 0) {
            if (toComp.getTypeWeapon().equals(this.weapon)) {
                if (toComp.getPrimary()) return 1;
                else return -1;
            }
            else
                return toComp.getTypeWeapon().compareTo(this.weapon);
        }
        else
           return diff;
    }
}
