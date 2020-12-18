package games;

import controllers.PlayAgent;
import org.junit.Test;
import starters.GBGBatch;
import starters.MTrainSweep;

import java.io.IOException;

public class GBGBatchTest extends GBGBatch {
    String selectedGame = "TicTacToe";
    String agtFile = "tdntuple3.agt.zip";
    String csvFile = "test.csv";
    String[] scaPar = GBGBatch.setDefaultScaPars(selectedGame);

    protected MTrainSweep mTrainSweep;

    @Test
    public void multiTrainAlphaSweep_T() {
        t_Game = GBGBatch.setupSelectedGame(selectedGame,scaPar);
        setupPaths(agtFile,csvFile);
        double[] alphaArr = {1.0};
        double[] alphaFinalArr = alphaArr.clone();

        boolean res = t_Game.loadAgent(0, filePath);
        assert res : "\n[GBGBatchTest] Aborted: agtFile = "+agtFile + " not found!";

        t_Game.m_xab.setTrainNumber(1);     // how many agents to train
        t_Game.m_xab.setGameNumber(4000);  // number of training episodes

        // run multiTrainAlphaSweep
        try {
            t_Game.m_xab.m_arena.taskState=Arena.Task.MULTTRN;
            t_Game.m_xfun.m_PlayAgents[0] = mTrainSweep.multiTrainAlphaSweep(0, alphaArr, alphaFinalArr,
                    t_Game, t_Game.m_xab, t_Game.getGameBoard(), csvFile);
            t_Game.m_xfun.m_PlayAgents[0].setAgentState(PlayAgent.AgentState.TRAINED);
            System.out.println("[GBGBatchTest] multiTrainAlphaSweep finished: Results written to "+csvFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void multiTrainLambdaSweep_T() {
        t_Game = GBGBatch.setupSelectedGame(selectedGame,scaPar);
        setupPaths(agtFile,csvFile);
        double[] lambdaArr = {0.5};

        boolean res = t_Game.loadAgent(0, filePath);
        assert res : "\n[GBGBatchTest] Aborted: agtFile = "+agtFile + " not found!";

        t_Game.m_xab.setTrainNumber(1);     // how many agents to train
        t_Game.m_xab.setGameNumber(4000);  // number of training episodes
        t_Game.m_xab.tdPar[0].setHorizonCut(0.1);

        // run multiTrainLambdaSweep
        try {
            t_Game.m_xab.m_arena.taskState=Arena.Task.MULTTRN;
            t_Game.m_xfun.m_PlayAgents[0] = mTrainSweep.multiTrainLambdaSweep(0, lambdaArr,
                    t_Game, t_Game.m_xab, t_Game.getGameBoard(), csvFile);
            t_Game.m_xfun.m_PlayAgents[0].setAgentState(PlayAgent.AgentState.TRAINED);
            System.out.println("[GBGBatchTest] multiTrainAlphaSweep finished: Results written to "+csvFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void multiTrainIncAmountSweep_T() {
    }
}