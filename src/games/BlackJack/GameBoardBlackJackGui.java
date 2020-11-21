package games.BlackJack;

import java.awt.Graphics;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import games.Arena;
import games.StateObservation;
import games.BlackJack.StateObserverBlackJack.BlackJackActionDet;
import tools.Types;

public class GameBoardBlackJackGui extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    GameBoardBlackJack m_gb = null;
    StateObserverBlackJack m_so = null;

    public GameBoardBlackJackGui(GameBoardBlackJack gb) {
        super("BlackJack");
        m_gb = gb;
    }

    JPanel actionZone, dealerZone, playerZone;

    public void initGui() {

        dealerZone = new JPanel();
        playerZone = new JPanel();
        actionZone = new JPanel();
        this.add(actionZone);
        this.add(playerZone);
        this.add(dealerZone);

        m_so = (StateObserverBlackJack) m_gb.getStateObs();

        actionZone.add(getActionZone(m_so));
        playerZone.add(playerPanel(m_so.getCurrentPlayer()));
        dealerZone.add(dealerPanel(m_so.getDealer()));

    }

    public void update(StateObserverBlackJack so, boolean withReset, boolean showValueOnGameboard) {
        clear();
        actionZone.add(getActionZone(so));
        playerZone.add(playerPanel(so.getCurrentPlayer()));
        dealerZone.add(dealerPanel(so.getDealer()));
        this.repaint();
    }

    public void clear() {
        dealerZone.removeAll();
        playerZone.removeAll();
        actionZone.removeAll();
    }

    public JLabel getCard(Card c) {
        return new JLabel(c.toString());
    }

    public JPanel getActionZone(StateObserverBlackJack so) {
        JPanel p = new JPanel();
        for (Types.ACTIONS a : so.getAllAvailableActions()) {
            String nameOfAction = BlackJackActionDet.values()[a.toInt()].name();
            p.add(new JButton(nameOfAction));
        }
        return p;
    }

    public JPanel getHandPanel(Hand h) {
        JPanel handPanel = new JPanel();
        for (Card c : h.getCards()) {
            handPanel.add(getCard(c));
        }
        return handPanel;
    }

    public JPanel playerPanel(Player p) {
        JPanel playerPanel = new JPanel();
        playerPanel.add(new JLabel(p.name));
        playerPanel.add(new JLabel("chips: " + p.getChips()));
        for (Hand h : p.getHands()) {
            playerPanel.add(getHandPanel(h));
        }
        return playerPanel;
    }

    public JPanel dealerPanel(Player dealer) {
        JPanel dealerPanel = new JPanel();
        dealerPanel.add(new JLabel("Dealer"));
        dealerPanel.add(getHandPanel(dealer.getActiveHand()));
        return dealerPanel;
    }

    public void showGameBoard(Arena arena, boolean alignToMain) {
        this.setVisible(true);
        if (alignToMain) {
            // place window with game board below the main window
            int x = arena.m_xab.getX() + arena.m_xab.getWidth() + 8;
            int y = arena.m_xab.getLocation().y;
            if (arena.m_ArenaFrame != null) {
                x = arena.m_ArenaFrame.getX();
                y = arena.m_ArenaFrame.getY() + arena.m_ArenaFrame.getHeight() + 1;
                this.setSize(1500, 1500);
            }
            this.setLocation(x, y);
        }
    }

    public void toFront() {
        super.setState(JFrame.NORMAL); // if window is iconified, display it normally
        super.toFront();
    }

    public void destroy() {
        this.setVisible(false);
        this.dispose();
    }

}
