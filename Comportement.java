package com.mycompany.mavenproject2;


/**
 *
 * @author Astrid
 */

public class Comportement {
    private HumeurBot etat ;
    
    public int confiantDiffFragsDeath = 10;
    public int confiantDiff25Frags = 5;
    public int decourageDiffDeathFragsLvl1 = 10;
    public int decourageHealthLevel = 20;
    public int decourageDiffDeathFragsLvl2 = 15;
    public int enrageDeath = 15;
    
    public Comportement(){
        double rand = Math.random();
        if (rand < 0.3) {
            this.etat = HumeurBot.Nerveux;
        }
        else {
            this.etat = HumeurBot.Neutre;
        }
    }
    
    
    public HumeurBot getHumeurBot(){
        return this.etat;
    }
    
    public void estConfiant(){
        this.etat = HumeurBot.Confiant;
    }
    
    public void estEnrage(){
        this.etat = HumeurBot.Enrage;
    }
    
    public void estNerveux(){
        this.etat = HumeurBot.Nerveux;
    }
    
    public void estNeutre(){
        this.etat = HumeurBot.Neutre;
    }
    
    public void estDecourage(){
        this.etat = HumeurBot.Decourage;
    }
    
    public void changementHumeurBot(int deaths, int frags, int healthLevel){
        // Le bot perd la partie? -> il se décourage
        if (deaths - frags > decourageDiffDeathFragsLvl2){
            estDecourage();
            return;
        }
        // Le bot est mort de nombreuse fois et est sur le point de se faire tuer? -> il se décourage
        if (deaths - frags > decourageDiffDeathFragsLvl1 && healthLevel < decourageHealthLevel){
            estDecourage();
            return;
        }
        // Le bot a un assez bon score pour gagner la partie? -> il est confiant
        if (25 - frags < confiantDiff25Frags || frags - deaths > confiantDiffFragsDeath){
            estConfiant();
            return;
        }
        // Le bot s'est ffait tur souvent? -> il est enragé!
        if (deaths > enrageDeath) {
            estEnrage();
            return;
        }
    }
    
    
}
