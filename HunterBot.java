package com.mycompany.botcontest;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import cz.cuni.amis.introspection.java.JProp;
import cz.cuni.amis.pogamut.base.agent.navigation.IPathExecutorState;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.utils.Pogamut;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.base.utils.math.DistanceUtils;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.base3d.worldview.object.Rotation;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.AdrenalineCombo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weapon;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.TabooSet;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.NavigationState;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004PathAutoFixer;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004DistanceStuckDetector;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004PositionStuckDetector;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004TimeStuckDetector;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.AddInventory;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Configuration;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Move;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.RemoveRay;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Rotate;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Stop;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.StopShooting;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.AutoTraceRay;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotDamaged;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.IncomingProjectile;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ItemPickedUp;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerDamaged;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerKilled;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.pogamut.ut2004.utils.UnrealUtils;
import cz.cuni.amis.utils.collections.MyCollections;
import cz.cuni.amis.utils.exception.PogamutException;
import cz.cuni.amis.utils.flag.FlagListener;
import java.util.Collection;
import java.util.Map;
import javax.vecmath.Vector3d;

/**
 * Example of Simple Pogamut bot, that randomly walks around the map searching
 * for preys shooting at everything that is in its way.
 *
 * @author Rudolf Kadlec aka ik
 * @author Jimmy
 */
@AgentScoped
public class HunterBot extends UT2004BotModuleController<UT2004Bot> {
    
    /* module pour gérer les combos d'adrénaline */
//    protected OurAdrenalineCombo adrenalineCombo;
//    protected Comportement comportementBot = new Comportement();
    
    /**
	 * Initializes memory/command modules of the bot.
	 * 
	 * @param bot
	 */
    @Override
    protected void initializeModules(UT2004Bot bot) {
        super.initializeModules(bot);
//        adrenalineCombo = new OurAdrenalineCombo(bot, info);
    }
    
    // Constants for rays' ids. It is allways better to store such values
    // in constants instead of using directly strings on multiple places of your
    // source code
    protected static final String LEFTBAS = "leftBasRay";
    protected static final String LEFTSHORT = "leftShort";
    protected static final String RIGHTBAS = "rightBasRay";
    protected static final String RIGHTSHORT = "rightShort";
    private AutoTraceRay leftbas , rightbas, leftshort, rightshort;
    
     /**
     * Flag indicating that the bot has been just executed.
     */
    private boolean first = true;
    private boolean raysInitialized = false;
    /**
     * Whether the left45 sensor signalizes the collision. (Computed in the
     * doLogic()) <p><p> Using {@link RaycastingBot#LEFT45} as the key for the
     * ray.
     */
    @JProp
    private boolean sensorLeftBas = false;
    private boolean sensorLeftShort = false;
    /**
     * Whether the right45 sensor signalizes the collision. (Computed in the
     * doLogic()) <p><p> Using {@link RaycastingBot#RIGHT45} as the key for the
     * ray.
     */
    @JProp
    private boolean sensorRightBas = false;
    private boolean sensorRightShort = false;
    /**
     * Whether the front sensor signalizes the collision. (Computed in the
     * doLogic()) <p><p> Using {@link RaycastingBot#FRONT} as the key for the
     * ray.
     */
    //@JProp
    //private boolean sensorFront = false;
  //  private boolean sensorFrontHaut = false;
    /**
     * Whether the bot is moving. (Computed in the doLogic())
     */
    @JProp
    private boolean moving = false;
    /**
     * Whether any of the sensor signalize the collision. (Computed in the
     * doLogic())
     */
    @JProp
    private boolean sensor = false;
    /**
     * boolean switch to activate engage behavior
     */
    @JProp
    public boolean shouldEngage = true;
    /**
     * boolean switch to activate pursue behavior
     */
    @JProp
    public boolean shouldPursue = true;
    /**
     * boolean switch to activate rearm behavior
     */
    @JProp
    public boolean shouldRearm = true;
    /**
     * boolean switch to activate collect health behavior
     */
    @JProp
    public boolean shouldCollectHealth = true;
    /**
     * how low the health level should be to start collecting health items
     */
    @JProp
    public int healthLevel = 75;
    /**
     * how low the health level should be to start running away from the fight
     */
    @JProp
    public int criticalHealthLevel = 20;
    /**
     * how many bot the hunter killed other bots (i.e., bot has fragged them /
     * got point for killing somebody)
     */
    @JProp
    public int frags = 0;
    /**
     * how many times the hunter died
     */
    @JProp
    public int deaths = 0;
    
    /* Liste intelligente d'armes a preferer pour le bot */
    private WeaponList weaponsPriority;

    /**
     * {@link PlayerKilled} listener that provides "frag" counting + is switches
     * the state of the hunter.
     *
     * @param event
     */
    @EventListener(eventClass = PlayerKilled.class)
    public void playerKilled(PlayerKilled event) {
        if (event.getKiller().equals(info.getId())) {
            ++frags;
        }
        if (enemy == null) {
            return;
        }
        if (enemy.getId().equals(event.getId())) {
            enemy = null;
        }
         /* maj des probabilites d'efficacite d'une arme */
        weaponsPriority.majWeapon(weaponry.getItemTypeForId(info.getCurrentWeapon()), true);
        
        /* ajout d'arme voulue a weaponPref pour que le bot l'utilise en priorite */
        if (weaponry.getLoadedWeapons().values() != null) {
            WeaponIA newWeapon = weaponsPriority.getNextWeapon(weaponry.getLoadedWeapons().values());
            weaponPrefs.addGeneralPref(newWeapon.getTypeWeapon(), newWeapon.getPrimary());
        }
    }
	
    /**
     * Used internally to maintain the information about the bot we're currently
     * hunting, i.e., should be firing at.
     */
    protected Player enemy = null;
    /**
     * Item we're running for. 
     */
    protected Item item = null;
    /**
     * Taboo list of items that are forbidden for some time.
     */
    protected TabooSet<Item> tabooItems = null;
    
    private UT2004PathAutoFixer autoFixer;
    
	private static int instanceCount = 0;

    /**
     * Bot's preparation - called before the bot is connected to GB2004 and
     * launched into UT2004.
     * @param bot
     */
    @Override
    public void prepareBot(UT2004Bot bot) {
        tabooItems = new TabooSet<Item>(bot);

        autoFixer = new UT2004PathAutoFixer(bot, navigation.getPathExecutor(), fwMap, aStar, navBuilder); // auto-removes wrong navigation links between navpoints

        // listeners        
        navigation.getState().addListener(new FlagListener<NavigationState>() {

            @Override
            public void flagChanged(NavigationState changedValue) {
                switch (changedValue) {
                    case PATH_COMPUTATION_FAILED:
                    case STUCK:
                        if (item != null) {
                            tabooItems.add(item, 10);
                        }
                        reset();
                        break;

                    case TARGET_REACHED:
                        //reset();
                        break;
                }
            }
        });
        
                
        /* initialisation de la liste de preference d'armes */
        weaponsPriority = new WeaponList();


        // FIRST we DEFINE GENERAL WEAPON PREFERENCES
        weaponPrefs.addGeneralPref(UT2004ItemType.MINIGUN, false);
        weaponPrefs.addGeneralPref(UT2004ItemType.LINK_GUN, false);
        weaponPrefs.addGeneralPref(UT2004ItemType.LIGHTNING_GUN, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.SHOCK_RIFLE, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.ROCKET_LAUNCHER, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.ASSAULT_RIFLE, true);        
        weaponPrefs.addGeneralPref(UT2004ItemType.FLAK_CANNON, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.BIO_RIFLE, true);
		
        // AND THEN RANGED
        //weaponPrefs.newPrefsRange(80).add(UT2004ItemType.SHIELD_GUN, true);
        
        weaponPrefs.newPrefsRange(400)
                .add(UT2004ItemType.FLAK_CANNON, true)
                .add(UT2004ItemType.MINIGUN, true)
                .add(UT2004ItemType.LINK_GUN, true)
                .add(UT2004ItemType.SHOCK_RIFLE, false)
                .add(UT2004ItemType.LIGHTNING_GUN, true)
                .add(UT2004ItemType.ASSAULT_RIFLE, true);

        weaponPrefs.newPrefsRange(800)
                .add(UT2004ItemType.ROCKET_LAUNCHER, true)
                .add(UT2004ItemType.MINIGUN, false)
                .add(UT2004ItemType.FLAK_CANNON, false)
                .add(UT2004ItemType.LINK_GUN, false)
                .add(UT2004ItemType.SHOCK_RIFLE, true)
                .add(UT2004ItemType.LIGHTNING_GUN, true)
                .add(UT2004ItemType.ASSAULT_RIFLE, true);

        weaponPrefs.newPrefsRange(100000)
                .add(UT2004ItemType.LIGHTNING_GUN, true)
                .add(UT2004ItemType.SHOCK_RIFLE, true); 
    }
    
    private void sayGlobal(String msg) {
    	// Simple way to send msg into the UT2004 chat
    	body.getCommunication().sendGlobalTextMessage(msg);
    	// And user log as well
    	log.info(msg);
    }

    /**
     * Here we can modify initializing command for our bot.
     *
     * @return
     */
    @Override
    public Initialize getInitializeCommand() {
        // just set the name of the bot and his skill level, 1 is the lowest, 7 is the highest
    	// skill level affects how well will the bot aim
        return new Initialize().setName("Hunter-" + (++instanceCount)).setDesiredSkill(6);
    }

    @Override
    public void botInitialized(GameInfo gameInfo, ConfigChange currentConfig, InitedMessage init) {
    	// By uncommenting line below, you will see all messages that goes trough GB2004 parser (GB2004 -> BOT communication)
    	//bot.getLogger().getCategory("Parser").setLevel(Level.ALL);
        // initialize rays for raycasting
        final int rayLength = (int) (UnrealUtils.CHARACTER_COLLISION_RADIUS * 20);
        final int rayShortLength = 125 ;
        final int rayBasLength = 210 ;
        // settings for the rays
        boolean fastTrace = true;        // perform only fast trace == we just need true/false information
        boolean floorCorrection = false; // provide floor-angle correction for the ray (when the bot is running on the skewed floor, the ray gets rotated to match the skew)
        boolean traceActor = false;      // whether the ray should collid with other actors == bots/players as well

        // 1. remove all previous rays, each bot starts by default with three
        // rays, for educational purposes we will set them manually
        getAct().act(new RemoveRay("All"));

        // 2. create new rays
        raycasting.createRay(LEFTBAS,  new Vector3d(0, -1, -0.3), rayBasLength, fastTrace, floorCorrection, traceActor);
        raycasting.createRay(RIGHTBAS, new Vector3d(0, 1, -0.3), rayBasLength, fastTrace, floorCorrection, traceActor);
        raycasting.createRay(LEFTSHORT,  new Vector3d(0, -1, 0), rayShortLength, fastTrace, floorCorrection, traceActor);
        raycasting.createRay(RIGHTSHORT, new Vector3d(0, 1, 0), rayShortLength, fastTrace, floorCorrection, traceActor);
        // register listener called when all rays are set up in the UT engine
        raycasting.getAllRaysInitialized().addListener(new FlagListener<Boolean>() {
            public void flagChanged(Boolean changedValue) {
                // once all rays were initialized store the AutoTraceRay objects
                // that will come in response in local variables, it is just
                // for convenience
                leftbas = raycasting.getRay(LEFTBAS);
                leftshort = raycasting.getRay(LEFTSHORT);
                rightbas = raycasting.getRay(RIGHTBAS);
                rightshort = raycasting.getRay(RIGHTSHORT);
            }
        });
        // have you noticed the FlagListener interface? The Pogamut is often using {@link Flag} objects that
        // wraps some iteresting values that user might respond to, i.e., whenever the flag value is changed,
        // all its listeners are informed

        // 3. declare that we are not going to setup any other rays, so the 'raycasting' object may know what "all" is        
        raycasting.endRayInitSequence();
        
        // IMPORTANT:
        // The most important thing is this line that ENABLES AUTO TRACE functionality,
        // without ".setAutoTrace(true)" the AddRay command would be useless as the bot won't get
        // trace-lines feature activated
        getAct().act(new Configuration().setDrawTraceLines(true).setAutoTrace(true));
        
        
         /* ajout des armes de bases dans la liste*/
        for (Item weapon : items.getAllItems(ItemType.Category.WEAPON).values()) {
            weaponsPriority.addWeapon(weapon.getType());
        }
        weaponsPriority.addWeapon(UT2004ItemType.ASSAULT_RIFLE);
        weaponsPriority.addWeapon(UT2004ItemType.SHIELD_GUN);
    }
        
    
    /**
     * Resets the state of the Hunter.
     */
    protected void reset() {
    	item = null;
        enemy = null;
        navigation.setFocus(null);
        navigation.stopNavigation();
        itemsToRunAround = null;
    }
    
    @EventListener(eventClass=PlayerDamaged.class)
    public void playerDamaged(PlayerDamaged event) {
    	log.info("I have just hurt other bot for: " + event.getDamageType() + "[" + event.getDamage() + "]");
    }
    
    @EventListener(eventClass=BotDamaged.class)
    public void botDamaged(BotDamaged event) {
       if( event.isBulletHit() ) {
           this.stateHit();
           log.info("I have just been hurt by other bot for: " + event.getDamageType() + "[" + event.getDamage() + "]");
       }
       if (event.isCausedByWorld()){
           if (info.isShooting() || info.isSecondaryShooting()) {
                // stop shooting
                getAct().act(new StopShooting());
           }
           move.doubleJump();
           stateMedKit();
           log.info("World damage " + event.getDamageType() + "[" + event.getDamage() + "]");
       }
    }
    
    /* listener active lorsque le bot ramasse un item */ 
    @EventListener(eventClass=ItemPickedUp.class)
    public void itemPickedUp(ItemPickedUp event) {
        
    }

    /**
     * Main method that controls the bot - makes decisions what to do next. It
     * is called iteratively by Pogamut engine every time a synchronous batch
     * from the environment is received. This is usually 4 times per second - it
     * is affected by visionTime variable, that can be adjusted in GameBots ini
     * file in UT2004/System folder.
     *
     * @throws cz.cuni.amis.pogamut.base.exceptions.PogamutException
     */
    @Override
    public void logic() {
//        HumeurBot previousHumeur = comportementBot.getHumeurBot();
//        comportementBot.changementHumeurBot(deaths, frags, healthLevel);
//        HumeurBot newHumeur = comportementBot.getHumeurBot();
        
        // Comportement NEUTRE

            
            if (info.getHealth() < criticalHealthLevel) {
                if (info.isShooting() || info.isSecondaryShooting()) {
                    getAct().act(new StopShooting());
                }
                this.stateMedKit();
                //return;
            }

            // 1) do you see enemy? 	-> go to PURSUE (start shooting / hunt the enemy)
            if (shouldEngage && players.canSeeEnemies() && weaponry.hasLoadedWeapon()) {
                stateEngage();
                return;
            }

            // 2) are you shooting? 	-> stop shooting, you've lost your target
            if (info.isShooting() || info.isSecondaryShooting()) {
                getAct().act(new StopShooting());
            }

            // 3) are you being shot? 	-> go to HIT (turn around - try to find your enemy)
            if (senses.isBeingDamaged()) {
                this.stateHit();
                return;
            }

            // 4) have you got enemy to pursue? -> go to the last position of enemy
            if (enemy != null && shouldPursue && weaponry.hasLoadedWeapon()) {  // !enemy.isVisible() because of 2)
                this.statePursue();
                return;
            }

            // 5) are you hurt?			-> get yourself some medKit
            if (shouldCollectHealth && info.getHealth() < healthLevel) {
                this.stateMedKit();
                return;
            }
            
            if (passingByItem()){
                this.statePassingByItem() ;
            }
            // 6) if nothing ... run around items
            stateRunAroundItems();
        //}
    }
    
    //////////////////
    // STATE MEDKIT //
    //////////////////
    protected void stateMedKit() {
        //log.info("Decision is: MEDKIT");
        Item item = items.getPathNearestSpawnedItem(ItemType.Category.HEALTH);
        if (item == null) {
        	log.warning("NO HEALTH ITEM TO RUN TO => ITEMS");
        	stateRunAroundItems();
        } else {
        	bot.getBotName().setInfo("MEDKIT");
        	navigation.navigate(item);
                this.item = item;
        }
    }
    
    //////////////////
    // STATE ENGAGE //
    //////////////////
    protected boolean runningToPlayer = false;

    /**
     * Fired when bot see any enemy. <ol> <li> if enemy that was attacked last
     * time is not visible than choose new enemy <li> if enemy is reachable and the bot is far - run to him
     * <li> otherwise - stand still (kind a silly, right? :-)
     * </ol>
     */
    protected void stateEngage() {
        //log.info("Decision is: ENGAGE");
        //config.setName("Hunter [ENGAGE]");

        boolean shooting = false ;
        double distance = Double.MAX_VALUE;
        pursueCount = 0;
        // if the rays are not initialized yet, do nothing and wait for their initialization 
        if (!raycasting.getAllRaysInitialized().getFlag()) {
            return;
        }
        sensorLeftBas = leftbas.isResult();
        sensorRightBas = rightbas.isResult();
        sensorLeftShort = leftshort.isResult();
        sensorRightShort = rightshort.isResult();

        // 1) pick new enemy if the old one has been lost
        if (enemy == null || !enemy.isVisible()) {
            // pick new enemy
            enemy = players.getNearestVisiblePlayer(players.getVisibleEnemies().values());
            if (enemy == null) {
                log.info("Can't see any enemies... ???");
                return;
            }
        }

        // 2) stop shooting if enemy is not visible
        if (!enemy.isVisible()) {
            if (info.isShooting() || info.isSecondaryShooting()) {
                // stop shooting
                getAct().act(new StopShooting());
            }
            runningToPlayer = false;
        } else {
            // 2) or shoot on enemy if it is visible
            
            
            //Tir dans la tete si le lighting gun est current weapon
            boolean headshot = random.nextFloat() > 0.6 ;
            if (headshot && weaponry.hasWeapon(UT2004ItemType.LIGHTNING_GUN) && weaponry.hasLoadedWeapon(UT2004ItemType.LIGHTNING_GUN) && (weaponry.getCurrentWeapon().getType()==UT2004ItemType.LIGHTNING_GUN)) {
                if (shoot.shoot(weaponry.getCurrentWeapon(), true, enemy.getLocation().addZ(40))) { 
                    log.info("Shooting lighting at enemy's head");
                    shooting = true;
                }
            } else {
                //Tir dans les pieds si le lance rocket est current weapon
                if (weaponry.hasWeapon(UT2004ItemType.ROCKET_LAUNCHER) && weaponry.hasLoadedWeapon(UT2004ItemType.ROCKET_LAUNCHER) && (weaponry.getCurrentWeapon().getType()==UT2004ItemType.ROCKET_LAUNCHER)) {
                    if (shoot.shoot(weaponry.getCurrentWeapon(), true, enemy.getLocation().addZ(-50))) { 
                        log.info("Shooting rockets at enemy's feet");
                        shooting = true;
                    }
                } else {
                    // try shock combo if the shock rifle is the current weapon
                    if (distance > 600 && weaponry.hasWeapon(UT2004ItemType.SHOCK_RIFLE) && weaponry.hasLoadedWeapon(UT2004ItemType.SHOCK_RIFLE) && (weaponry.getCurrentWeapon().getType()==UT2004ItemType.SHOCK_RIFLE)) {
                        shoot.shootSecondary(enemy);
                        if (seeIncomingProjectile()) {
                            IncomingProjectile proj = pickProjectile();
                            if (proj.getType().equals("XWeapons.ShockProjectile") && enemy.getLocation().getDistance(proj.getLocation()) < 400 ){
                                log.info("Shooting PROJECTILE");
                                shoot.shoot(proj.getId());
                            } else {
                                getAct().act(new StopShooting());
                            }
                        }
                        shooting = true;
                    } else {
                        // tir normal
                        if (shoot.shoot(weaponPrefs, enemy) != null) {
                        // if (shoot.shoot(weaponry.getCurrentWeapon(), true, enemy)) { // A CHECKER LE BOOLEEN 
                            log.info("Shooting at enemy!!!");
                            shooting = true;
                        }
                    }
                }
            }
        }
        
        distance = info.getLocation().getDistance(enemy.getLocation());
        if (enemy.isVisible() && distance < 1200) {
            float rand1 = random.nextFloat() ;
            float rand2 = random.nextFloat() ;
            boolean direction = true ; //true -> droite, false -> gauche
            if (rand1 > 0.5) {
                direction = false ;
            }
            if (rand2 > 0.8) {
                if ((!sensorRightShort) && sensorRightBas && (!sensorLeftShort) && sensorLeftBas ) {
                    if (direction) {
                        move.dodgeRight(enemy, false);
                    } else {
                        move.dodgeLeft(enemy, false);
                    }
                } else {
                    if ((!sensorRightShort) && sensorRightBas) {
                        move.dodgeRight(enemy, false);
                    } else {
                        if ((!sensorLeftShort) && sensorLeftBas) {
                            move.dodgeLeft(enemy, false);
                        } else {
                            move.doubleJump();
                        }
                    }
                }
            } else {
                if ((!sensorRightShort) && sensorRightBas && (!sensorLeftShort) && sensorLeftBas ) {
                    if (direction) {
                        move.strafeRight(200);
                    } else {
                        move.strafeLeft(200);
                    }
                } else {
                    if ((!sensorRightShort) && sensorRightBas) {
                        move.strafeRight(200);
                    } else {
                        if ((!sensorLeftShort) && sensorLeftBas) {
                            move.strafeLeft(200);
                        } else {
                            move.jump();
                        }
                    }
                }
            }
        }
        if (bot.getVelocity().isPlanarZero()){
            navigation.navigate(enemy);
            runningToPlayer = true;
        }

        // 3) if enemy is far or not visible - run to him
        int decentDistance = 1200 ;
        distance = info.getLocation().getDistance(enemy.getLocation());
        if (!enemy.isVisible() || !shooting || decentDistance < distance) { 
            if (!runningToPlayer) {
                navigation.navigate(enemy);
                runningToPlayer = true;
                
            }
        }
        item = null;
    }

    ///////////////
    // STATE HIT //
    ///////////////
    protected void stateHit() {
        //log.info("Decision is: HIT");
        bot.getBotName().setInfo("HIT");
        if (navigation.isNavigating()) {
            navigation.stopNavigation();
            item = null;
        }
        if (enemy == null){
            if (info.isShooting() || info.isSecondaryShooting()) {
                getAct().act(new StopShooting());
            }
            move.setRotationSpeed(new Rotation(6144, 240000, 4096));
            getAct().act(new Rotate().setAmount(32000));
        }
    }

    //////////////////
    // STATE PURSUE //
    //////////////////
    /**
     * State pursue is for pursuing enemy who was for example lost behind a
     * corner. How it works?: <ol> <li> initialize properties <li> obtain path
     * to the enemy <li> follow the path - if it reaches the end - set lastEnemy
     * to null - bot would have seen him before or lost him once for all </ol>
     */
    protected void statePursue() {
        //log.info("Decision is: PURSUE");
        ++pursueCount;
        if (pursueCount > 30) {
            reset();
        }
        if (enemy != null) {
            bot.getBotName().setInfo("PURSUE");
            if (!enemy.isVisible()) {     
                if (info.isShooting() || info.isSecondaryShooting())
                    getAct().act(new StopShooting());
                navigation.setFocus(null);
            } else 
                navigation.setFocus(enemy);
            navigation.navigate(enemy);
            item = null;
        } else {
            if (info.isShooting() || info.isSecondaryShooting())
                getAct().act(new StopShooting());
            reset();
        }
    }
    protected int pursueCount = 0;

    

    ////////////////////////////
    // STATE PASSING BY ITEM  //
    ////////////////////////////
    boolean navigatingToNearestItem = false;
    protected void statePassingByItem() {
            Item nearestItem  = items.getNearestSpawnedItem();
            if (isItemInterseting(nearestItem) && item != null && nearestItem != null && nearestItem != item && !navigatingToNearestItem){
                navigatingToNearestItem = true ;
                Item nextNearestItem = getNearestItemFromItem(nearestItem);
                if (nextNearestItem.getLocation().getDistance(nearestItem.getLocation()) > 500 || !isItemInterseting(nextNearestItem))
                    nextNearestItem = null;
                if (nearestItem.getLocation().getDistance(this.info.getLocation()) < item.getLocation().getDistance(this.info.getLocation())){
                    navigation.navigate(nearestItem);
                    if (nextNearestItem != null)
                        navigation.setContinueTo(nextNearestItem.getLocation());
                    else 
                        navigation.setContinueTo(item);
                } else {
                    navigation.navigate(item) ;
                }
                navigatingToNearestItem = false ;
            }
    }

    ////////////////////////////
    // STATE RUN AROUND ITEMS //
    ////////////////////////////
    protected List<Item> itemsToRunAround = null;
    protected void stateRunAroundItems() {
        //log.info("Decision is: ITEMS");
        //config.setName("Hunter [ITEMS]");
        if (navigation.isNavigatingToItem()) return;
        
        List<Item> interesting = new ArrayList<Item>();
        
        if (this.hasDecentWeapon()) {
            // ADD QUADS
            interesting.addAll(items.getSpawnedItems(UT2004ItemType.U_DAMAGE_PACK).values());
            interesting.addAll(items.getSpawnedItems(UT2004ItemType.SUPER_HEALTH_PACK).values());
            // ADD ARMORS
            for (ItemType itemType : ItemType.Category.ARMOR.getTypes()) {
                    interesting.addAll(items.getSpawnedItems(itemType).values());
            }
        }
       
        if (MyCollections.asList(tabooItems.filter(interesting)).isEmpty()){
            // ADD WEAPONS
            for (ItemType itemType : ItemType.Category.WEAPON.getTypes()) {
                    if (!weaponry.hasLoadedWeapon(itemType)) interesting.addAll(items.getSpawnedItems(itemType).values());
            }
            // ADD HEALTHS
            if (info.getHealth() < 100) {
                    interesting.addAll(items.getSpawnedItems(UT2004ItemType.HEALTH_PACK).values());
            }
            // ADD ADRENALINE
            interesting.addAll(items.getSpawnedItems(UT2004ItemType.ADRENALINE_PACK).values());
        }
        
        Item item ;
        if (!MyCollections.asList(tabooItems.filter(interesting)).isEmpty()){
            item = MyCollections.asList(tabooItems.filter(interesting)).get(0);
        } else {
            item = null ;
        }
        
        if (item == null) {
        	log.warning("NO ITEM TO RUN FOR!");
        	if (navigation.isNavigating()) return;
        	bot.getBotName().setInfo("RANDOM NAV");
        	navigation.navigate(navPoints.getRandomNavPoint());
        } else {
        	this.item = item;
        	log.info("RUNNING FOR: " + item.getType().getName());
        	bot.getBotName().setInfo("ITEM: " + item.getType().getName() + "");
        	navigation.navigate(item);
        }        
    }

    ////////////////
    // BOT KILLED //
    ////////////////
    @Override
    public void botKilled(BotKilled event) {
    	reset();
        /* maj des probabilites d'efficacite d'une arme */
        weaponsPriority.majWeapon(weaponry.getItemTypeForId(info.getCurrentWeapon()), false);
        
    }
    
        ///////////////////////////////////////////////////////////////

    private boolean seeIncomingProjectile() {
    	for (IncomingProjectile proj : world.getAll(IncomingProjectile.class).values()) {
    		if (proj.isVisible()) return true;
    	}
        return false;
    }
    
    private IncomingProjectile pickProjectile() {
            return DistanceUtils.getNearest(world.getAll(IncomingProjectile.class).values(), info.getLocation());
    }
    
    protected boolean hasDecentWeapon(){
        return (this.weaponry.hasWeapon(UT2004ItemType.BIO_RIFLE) || this.weaponry.hasWeapon(UT2004ItemType.FLAK_CANNON) || this.weaponry.hasWeapon(UT2004ItemType.LIGHTNING_GUN)
                || this.weaponry.hasWeapon(UT2004ItemType.LINK_GUN) || this.weaponry.hasWeapon(UT2004ItemType.MINIGUN) || this.weaponry.hasWeapon(UT2004ItemType.ROCKET_LAUNCHER)
                || this.weaponry.hasWeapon(UT2004ItemType.SHOCK_RIFLE));
    }
	
    protected boolean hasDecentAmmo(){
        if(weaponry.hasWeapon(UT2004ItemType.BIO_RIFLE) && weaponry.hasPrimaryWeaponAmmo(UT2004ItemType.BIO_RIFLE))
            return true ;
        if(weaponry.hasWeapon(UT2004ItemType.FLAK_CANNON) && weaponry.hasPrimaryWeaponAmmo(UT2004ItemType.FLAK_CANNON))
            return true ;
        if(weaponry.hasWeapon(UT2004ItemType.LIGHTNING_GUN) && weaponry.hasPrimaryWeaponAmmo(UT2004ItemType.LIGHTNING_GUN))
            return true ;
        if(weaponry.hasWeapon(UT2004ItemType.LINK_GUN) && weaponry.hasPrimaryWeaponAmmo(UT2004ItemType.LINK_GUN))
            return true ;
        if(weaponry.hasWeapon(UT2004ItemType.MINIGUN) && weaponry.hasPrimaryWeaponAmmo(UT2004ItemType.MINIGUN))
            return true ;
        if(weaponry.hasWeapon(UT2004ItemType.ROCKET_LAUNCHER) && weaponry.hasPrimaryWeaponAmmo(UT2004ItemType.ROCKET_LAUNCHER))
            return true ;
        if(weaponry.hasWeapon(UT2004ItemType.SHOCK_RIFLE) && weaponry.hasPrimaryWeaponAmmo(UT2004ItemType.SHOCK_RIFLE))
            return true ;
        return false ;
    }
    
    protected boolean passingByItem(){
        Item nearestItem = items.getPathNearestSpawnedItem();
        if (nearestItem != null)
            return (nearestItem.getLocation().getDistance(bot.getLocation()) < 500 && isItemInterseting(nearestItem));
        else 
            return false ;
    }
    
    protected boolean isItemInterseting(Item item){
        if (item.getType().getCategory().name().equals("ADRENALINE"))
            if (info.getAdrenaline() < 100)
                return true ;
        if (item.getType().getCategory().name().equals("AMMO"))
            return isAmmoInteresting(item) ;
        if (item.getType().getCategory().name().equals("ARMOR"))
            if (info.getArmor() < 100)
                return true ;
        if (item.getType().getCategory().name().equals("HEALTH"))
            return isHealthIntersting(item) ;
        if (item.getType().getCategory().name().equals("SHIELD"))
            if (info.getHealth() < 100)
                return true ;
        if (item.getType().getCategory().name().equals("WEAPON"))
            if (!weaponry.hasWeapon(item.getType()))
                return true ;
        return false ;
    }
    
    protected boolean isAmmoInteresting (Item item){
        if (item.getType().getCategory() != ItemType.Category.AMMO){
            return false ;
        } else {
            if (item.getType().getGroup().toString().equals("ASSAULT_RIFLE"))
                return weaponry.getAmmo(item.getType()) < 200 ;
            if (item.getType().getGroup().toString().equals("BIO_RIFLE"))
                return weaponry.getAmmo(item.getType()) < 50 ;
            if (item.getType().getGroup().toString().equals("FLAK_CANNON"))
                return weaponry.getAmmo(item.getType()) < 35 ;
            if (item.getType().getGroup().toString().equals("LIGHTNING_GUN"))
                return weaponry.getAmmo(item.getType()) < 40 ;
            if (item.getType().getGroup().toString().equals("LINK_GUN"))
                return weaponry.getAmmo(item.getType()) < 220 ;
            if (item.getType().getGroup().toString().equals("MINIGUN"))
                return weaponry.getAmmo(item.getType()) < 300 ;
            if (item.getType().getGroup().toString().equals("ROCKET_LAUNCHER"))
                return weaponry.getAmmo(item.getType()) < 30 ;
            if (item.getType().getGroup().toString().equals("SHOCK_RIFLE"))
                return weaponry.getAmmo(item.getType()) < 50 ;
            if (item.getType().getGroup().toString().equals("SNIPER_RIFLE"))
                return weaponry.getAmmo(item.getType()) < 40 ;
            return false ;
        }
    }
    
    protected boolean isHealthIntersting (Item item){
        if (item.getType().getCategory() != ItemType.Category.HEALTH){
            return false ;
        } else {
            if (item.getType().getGroup().toString().equals("HEALTH"))
                return info.getHealth() < 100 ;
            if (item.getType().getGroup().toString().equals("MINI_HEALTH"))
                return info.getHealth() < 199 ;
            return false ;
        }
    }
    
    protected Item getNearestItemFromItem(Item item){
        double distMin = 10000000;
        Item nearestItem = null ;
        Location itemLocation = item.getLocation();
        Collection<Item> spawnedItems = items.getSpawnedItems().values();
        for (Item spawnedItem : spawnedItems)
            if (itemLocation.getDistance(spawnedItem.getLocation()) < distMin && itemLocation.getDistance(spawnedItem.getLocation()) > 10 && isItemInterseting(spawnedItem)){
                distMin = itemLocation.getDistance(spawnedItem.getLocation());
                nearestItem = spawnedItem ;
            }
        return nearestItem;
    }
    
    ///////////////////////////////////
    public static void main(String args[]) throws PogamutException {
        // starts 3 Hunters at once
        String host = "localhost";
        int port = 3000;

        if (args.length > 0)
        {
                host = args[0];
        }
        if (args.length > 1)
        {
                String customPort = args[1];
                try
                {
                        port = Integer.parseInt(customPort);
                }
                catch (Exception e)
                {
                        System.out.println("Invalid port. Expecting numeric. Resuming with default port: "+port);
                }
        }     
    	new UT2004BotRunner(HunterBot.class, "Hunter", host, port).setMain(true).setLogLevel(Level.INFO).startAgents(1);
    }
}