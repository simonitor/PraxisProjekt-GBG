package games.RubiksCube;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import controllers.AgentBase;
import controllers.PlayAgent;
import games.Arena;
import games.StateObservation;
import params.ParOther;
import tools.ScoreTuple;
import tools.Types;
import tools.Types.ACTIONS;
import tools.Types.ACTIONS_VT;

/**
 *  Implements DAVI algorithm (Deep Approximate Value Iteration) for Rubik's Cube [Agnostelli2019].
 *  <p>
 *  It simplifies DAVI by replacing the neural net with state-based value iteration. It maintains a table or  
 *  {@link HashMap} of the value of all seen states. Thus, it is probably only viable 
 *  for smaller systems and cannot generalize to unseen states.<br>
 *  It <b>maximizes</b> the (non-positive) value V(s) where each step (twist) adds a negative step reward to V(s).  
 *  Only the solved cube s* has V(s*)=0.
 *
 */
public class DAVI2Agent extends AgentBase implements PlayAgent {

	private static final long serialVersionUID = 12L;

	private final static StateObserverCube def = new StateObserverCube();   // default (solved) cube
	
	private final static double LOW_V = -9.0;		// a low V-value for all states not present in HashMap

	/**
	 * HashMap for the V-values V(s)
	 */
	private final HashMap<String,Double> vm;

	private final Random rand;

	public DAVI2Agent(String name, ParOther oPar) {
		super(name, oPar);
		super.setMaxGameNum(1000);		
		super.setGameNum(0);		
		setAgentState(AgentState.INIT);
        rand = new Random(System.currentTimeMillis());
		vm = new HashMap<String, Double>();
	}
	
	/**
	 * After loading an agent from disk fill the param tabs of {@link Arena} according to the
	 * settings of this agent
	 * 
	 * @param n         fill the {@code n}th parameter tab
	 * @param m_arena	member {@code m_xab} has the param tabs
	 * 
	 * @see Arena#loadAgent
	 */
	public void fillParamTabsAfterLoading(int n, Arena m_arena) { 
		m_arena.m_xab.setOParFrom(n, this.getParOther() );
		System.out.println("DAVI2 vm.size="+this.vm.size());
	}
	
	@Override
	public ACTIONS_VT getNextAction2(StateObservation so, boolean random, boolean silent) {
		int i;
		StateObserverCube newSO;
        ACTIONS actBest;
        ArrayList<ACTIONS> acts = so.getAvailableActions();
        ArrayList<ACTIONS> bestActions = new ArrayList<>();
		double[] vTable = new double[acts.size()];
        double maxValue = -Double.MAX_VALUE;
        double value;

        assert so.isLegalState() : "Not a legal state"; 
        assert so instanceof StateObserverCube : "Not a StateObserverCube object";
        
        for(i = 0; i < acts.size(); ++i)
        {
        	newSO = ((StateObserverCube) so).copy();
        	newSO.advance(acts.get(i));
        	
        	// value is the V(s) for for taking action i in state s='so'. Action i leads to state newSO.
        	value = vTable[i] = newSO.getRewardTuple(false).scTup[0] +
								newSO.getStepRewardTuple().scTup[0] + daviValue(newSO);
        	// Always *maximize* 'value' 
        	if (value==maxValue) bestActions.add(acts.get(i));
        	if (value>maxValue) {
        		maxValue = value;
        		bestActions.clear();
        		bestActions.add(acts.get(i));
        	}
        } // for
        
        // There might be one or more than one action with minValue. 
        // Break ties by selecting one of them randomly:
        actBest = bestActions.get(rand.nextInt(bestActions.size()));

        // optional: print the best action
        if (!silent) {
        	newSO = ((StateObserverCube) so).copy();
        	newSO.advance(actBest);
        	System.out.println("---Best Move: "+newSO.stringDescr()+"   "+maxValue);
        }			

        double[] res = {maxValue};
        ScoreTuple scBest = new ScoreTuple(res);
        return new ACTIONS_VT(actBest.toInt(), false, vTable,maxValue,scBest);
	}

	/**
	 * This is the simple version: maintain a full hash table for all visited states. This is only viable for cubes 
	 * with not too large state spaces.
	 * @param so
	 * @return 0, if {@code so} is the solved state, LOW_V if {@code so} is unknown in the HashMap. In all
	 * 		   other cases, return the HashMap value of {@code so}.
	 */
	public double daviValue(StateObserverCube so) {
		if (so.isEqual(def)) return 0;			// Do not return LOW_V, but return 0 if the solved cube is found.
												// Thus the values stored in vm are 0.9, 0.8, 0.7, ...
												// Otherwise the solved cube would get a total value of -8.1 =
												// -9.1 + REWARD_POSITIVE, which is unpleasently negative for InspectV
		String stringRep = so.stringDescr();
		Double dvalue = vm.get(stringRep); 		// returns null if not in vm
		double x = (dvalue==null) ? LOW_V : dvalue;
		return x;
	}
	
    /**
     * Train the agent for one complete episode starting from state so
     * 
     * @param so 		start state
	 * @return			true, if agent raised a stop condition (only CMAPlayer - deprecated)	 
     */
    @Override
	public boolean trainAgent(StateObservation so) {
		Types.ACTIONS_VT  a_t;
		StateObservation s_t = so.copy();
		int epiLength = m_oPar.getEpisodeLength();
		assert (epiLength != -1) : "trainAgent: Rubik's Cube should not be run with epiLength==-1 !";
		if (so.equals(def)) {
			System.err.println("[DAVI2.trainAgent] cube should NOT be the default (solved) cube!");
			return false;
		}
		boolean m_finished = false;
		
		do {
	        m_numTrnMoves++;		// number of train moves 
	        
			a_t = getNextAction2(s_t.partialState(), true, true);	// choose action a_t (agent-specific behavior)
	        // put the best V-table value for state s_t into the HashMap
			if (a_t.getVBest()>CubeConfig.stepReward+LOW_V)
				// if V(s) <= c+L, there is no need to store it --> results in a factor 6 smaller hash map.
	        	vm.put(s_t.stringDescr(), a_t.getVBest());
	        
			//System.out.println(s_t.stringDescr()+", "+a_t.getVBest());
	        
			s_t.advance(a_t);		// advance the state 

			if (s_t.isGameOver()) m_finished = true;
			if (s_t.getMoveCounter()>=epiLength) m_finished=true;

		} while(!m_finished);			
		//System.out.println("Final state: "+s_t.stringDescr()+", "+a_t.getVBest());
				
		incrementGameNum();
		if (this.getGameNum() % 2000 == 0) System.out.println("gameNum: "+this.getGameNum());
		
		return false;		
	} 

	@Override
	public double getScore(StateObservation so) {
        assert (so instanceof StateObserverCube) : "Not a StateObserverCube object";
		return daviValue((StateObserverCube) so);
	}

	@Override
	public ScoreTuple getScoreTuple(StateObservation so, ScoreTuple prevTuple) {
		double[] d = {daviValue((StateObserverCube)so)};
		return new ScoreTuple(d);
	}

	@Override
	public ScoreTuple estimateGameValueTuple(StateObservation so, ScoreTuple prevTuple) {
		double[] d = {daviValue((StateObserverCube)so)};
		return new ScoreTuple(d);

		// this variant is another possibility, it adds one more ply, should be more accurate, but it is slower
//		double[] zero = {0.0};
//		if (so.equals(def)) return new ScoreTuple(zero);			// important bug fix! /WK/2020/05
//		ACTIONS_VT a_t = this.getNextAction2(so.partialState(), false, true);
//		return a_t.getScoreTuple();
	}

	@Override
	public boolean isTrainable() {
		return true;
	}

    @Override
	public String printTrainStatus() {
		return getClass().getSimpleName()+": pMax="+CubeConfig.pMax + ", vm.size="+vm.size(); 
	}

    @Override
	public String stringDescr2() {
		return getClass().getName() + ": pMax="+CubeConfig.pMax + ", epiLength="+m_oPar.getEpisodeLength()
									+ ", vm.size="+vm.size();
	}

}
