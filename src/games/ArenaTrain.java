package games;

import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingConstants;

import controllers.AgentBase;
import controllers.PlayAgent;
import controllers.PlayAgent.AgentState;
import games.Arena.Task;
import gui.MessageBox;
import gui.StatusBar;
import tools.Types;


/**
 * This class contains the GUI for the arena with train capabilities.
 * It extends the task dispatcher of {@link Arena} with the function 
 * {@link #performArenaDerivedTasks()} which contains tasks to trigger functions for
 * agent learning, parameterization, inspection and so on. <p>
 * The functions are implemented in {@link XArenaFuncs}.
 * The GUI for buttons and choice boxes is in {@link XArenaButtons}. <p>
 *
 * Run this class as main (from one of the derived classes {@code ArenaTrain*}).
 * 
 * @see XArenaButtons 
 * @see XArenaFuncs
 * 
 * @author Wolfgang Konen, TH Koeln, Nov'16
 *
 */
abstract public class ArenaTrain extends Arena  
{
	private static final long serialVersionUID = 1L;
	private Thread playThreadAT = null;

	// --- never used ---
//	// launch ArenaTrain with UI
//	public ArenaTrain() {
//		super();
//		initArenaTrain();
//	}
//	
//	// launch ArenaTrain with UI
//	public ArenaTrain(String title) {
//		super(title);
//		initArenaTrain();
//	}
	
	// decide via withUI whether wit UI or not
	public ArenaTrain(String title, boolean withUI) {
		super(title, withUI);
		initArenaTrain();
	}
	
	private void initArenaTrain() {
		if (hasGUI()) {
			this.setTitle("ArenaTrain  "+this.getGameName());
			this.m_xab.initTrain();
		}
	}
	
	public boolean hasTrainRights() {
		return true;
	}
	
	public int getGuiArenaHeight() {
		return Types.GUI_ARENATRAIN_HEIGHT;
	}
	
	/**
	 * This method uses the member taskState from parent class {@link Arena}, 
	 * performs several actions only appropriate for {@link ArenaTrain} 
	 * and - importantly - changes taskState back to IDLE (when appropriate).
	 * <p>
	 * A class derived from {@link ArenaTrain} may override this method, but it 
	 * should usually call inside with {@code super.performArenaDerivedTask()} this method, 
	 * before extensions are added.
	 * 
	 * @see Arena
	 */
	@Override
	public void performArenaDerivedTasks() {
		String agentN;
		int n;
		switch (taskState) {
		case TRAIN: 
			n = m_xab.getNumTrainBtn();
			agentN = m_xab.getSelectedAgent(n);
			PlayAgent pa=null;
			try {
				pa = m_xfun.constructAgent(n,agentN, m_xab);
				if (pa==null) throw new RuntimeException("Could not construct agent = " + agentN);
				
			} catch(Exception e) {
				this.showMessage(e.getMessage(),"Warning", JOptionPane.WARNING_MESSAGE);
			} 
			if (pa!=null && pa.isTrainable()) {
//				enableButtons(false);	// see mTrain[n].addActionListener in XArenaButtonsGui
				setStatusMessage("Training "+agentN+"-Player X ...");
				try {
					
					m_xfun.m_PlayAgents[n] = m_xfun.train(n,agentN, m_xab, gb);
					
				} catch (RuntimeException e) {
					String s = e.getMessage();
					e.printStackTrace();
					if (s==null) s=e.getClass().getName();
					this.showMessage(s,"Error", JOptionPane.ERROR_MESSAGE);
					enableButtons(true);
					taskState = Task.IDLE; 
					break;
				}

				if (m_xfun.m_PlayAgents[n] != null) {
					pa = m_xfun.m_PlayAgents[n];
					Evaluator m_evaluator2 = makeEvaluator(pa,gb,0,m_xab.oPar[n].getQuickEvalMode(),1);
					m_evaluator2.eval(pa);
					System.out.println("final "+m_evaluator2.getMsg());
					m_xfun.m_PlayAgents[n].setAgentState(AgentState.TRAINED);
					setStatusMessage("final "+m_evaluator2.getMsg());
					//System.out.println("Duration training: " + ((double)pa.getDurationTrainingMs()/1000));
					//System.out.println("Duration evaluation: " + ((double)pa.getDurationEvaluationMs()/1000));
				} else {
					setStatusMessage("Done.");
				}

			}
			enableButtons(true);
			taskState = Task.IDLE; 
			break;
		case MULTTRN:
//			enableButtons(false);	// see MultiTrain.addActionListener in XArenaButtonsGui
			
	        setStatusMessage("MultiTrain started ...");
	        long start_time = Calendar.getInstance().getTime().getTime();
			try {
				
				m_xfun.m_PlayAgents[0] = m_xfun.multiTrain(0, m_xab.getSelectedAgent(0), m_xab, gb, "multiTrain.csv");
			
			} catch (RuntimeException e) {
				this.showMessage(e.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
				enableButtons(true);
				taskState = Task.IDLE; 
				break;
			}
			
			if (m_xfun.m_PlayAgents[0]==null) {
		        setStatusMessage("Done.");
			} else {
				m_xfun.m_PlayAgents[0].setAgentState(AgentState.TRAINED);
		        setStatusMessage("MultiTrain finished: "+ m_xfun.getLastMsg());
			}
	        long elapsed_time = Calendar.getInstance().getTime().getTime() - start_time;
	        System.out.println("MultiTrain finished, time : "+ elapsed_time + " msec");

			enableButtons(true);
			taskState = Task.IDLE; 
			updateBoard();
			break;	
//		case INSPECTNTUP:
//			gb.clearBoard(false,true);
//			InspectNtup();
//			state = Task.IDLE; 
//			break;
		
		}
	
	}
	
// *TODO* --- this may be integrated later in the general interface ---
//
//	/**
//	 * Inspect the N-tuple states and their LUT weights for agents using N-tuples 
//	 * (currently {@link TDSNPlayer}, {@link TD_NTPlayer} or (deprecated) {@link TDSPlayer} with featmode==8).
//	 * <p>
//	 * Based on the current N-tuple situation, construct and display a {@link NTupleShow} object.
//	 * 
//	 * @see NTupleShow
//	 * @see TicGameButtons#nTupShowAction()
//	 */
//	protected void InspectNtup() {
//		String pa_string = m_TTT.m_PlayAgentX.getClass().getName();
//		System.out.println("[InspectNtup] "+pa_string);
//		NTuple[] nTuples = null;
//		if (pa_string=="TicTacToe.TDSNPlayer") {
//			nTuples = ((TDSNPlayer) m_TTT.m_PlayAgentX).getNTuples();
//		}
//		if (pa_string=="TicTacToe.TD_NTPlayer") {
//			((TD_NTPlayer) m_TTT.m_PlayAgentX).copyWeights();
//			nTuples = ((TD_NTPlayer) m_TTT.m_PlayAgentX).getNTuples();
//		}
//		if (pa_string=="controllers.TD.TDPlayerTTT" && m_TTT.m_PlayAgentX.getFeatmode()==8) {
//			((TDSPlayer) m_TTT.m_PlayAgentX).copyWeights();
//			nTuples = ((TicTDBase) m_TTT.m_PlayAgentX).getNTuples();
//		}
//		if (nTuples!=null) {
//			m_xab.ntupleShow = new NTupleShow(nTuples,m_xab);
//			m_xab.nTupShowAction();
//		}
//		else System.out.println("[InspectNtup] Warning: nTuples==null!");
//
//	}
	
}


