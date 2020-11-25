package games.BlackJack;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.plaf.DimensionUIResource;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import games.Arena;
import games.StateObservation;
import games.BlackJack.StateObserverBlackJack.BlackJackActionDet;
import params.GridLayout2;
import tools.Types;

public class GameBoardBlackJackGui extends JFrame {

    class ActionHandler implements ActionListener {
        int action;

        ActionHandler(int action) {
            this.action = action;
        }

        public void actionPerformed(ActionEvent e) {
        }
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    GameBoardBlackJack m_gb = null;
    StateObserverBlackJack m_so = null;

    public GameBoardBlackJackGui(GameBoardBlackJack gb) {
        super("BlackJack");
        m_gb = gb;
        initGui();
    }

    JPanel actionZone, dealerZone, playerZone;

    public void initGui() {

        JPanel window = new JPanel();
        GridLayout grid = new GridLayout(0, 3);
        window.setLayout(grid);

        dealerZone = new JPanel();
        playerZone = new JPanel();
        actionZone = new JPanel();

        dealerZone.setBackground(new Color(100, 1, 1));
        playerZone.setBackground(new Color(1, 100, 1));
        actionZone.setBackground(new Color(1, 1, 100));

        window.add(actionZone);
        window.add(playerZone);
        window.add(dealerZone);

        m_so = (StateObserverBlackJack) m_gb.getStateObs();
        actionZone.setLayout(new GridLayout(2, 1));
        actionZone.add(getActionZone(m_so));
        playerZone.add(playerPanel(m_so.getPlayers()[0]));
        dealerZone.add(dealerPanel(m_so.getDealer()));
        actionZone.add(handHistoryPanel(m_so));
        this.add(window);
        this.setVisible(true);
        this.repaint();

    }

    public void update(StateObserverBlackJack so, boolean withReset, boolean showValueOnGameboard) {
        clear();
        actionZone.add(getActionZone(so));
        playerZone.add(playerPanel(so.getPlayers()[0]));
        dealerZone.add(dealerPanel(so.getDealer()));
        actionZone.add(handHistoryPanel(so));
        this.setVisible(true);
        this.repaint();

    }

    public void clear() {
        dealerZone.removeAll();
        playerZone.removeAll();
        actionZone.removeAll();
    }

    public JLabel getCard(Card c) {
        JLabel cardLabel = new JLabel(c.toString());
        if (c.suit.equals(Card.Suit.DIAMOND) || c.suit.equals(Card.Suit.HEART)) {
            cardLabel.setForeground(new Color(255, 50, 0));
        }
        return cardLabel;
    }

    public JPanel getActionZone(StateObserverBlackJack so) {
        JPanel p = new JPanel();
        p.setLayout(new GridLayout2(so.getAllAvailableActions().size(), 1));
        for (Types.ACTIONS a : so.getAvailableActions()) {
            String nameOfAction = BlackJackActionDet.values()[a.toInt()].name();
            JButton buttonToAdd = new JButton(nameOfAction);
            buttonToAdd.addActionListener(new ActionHandler(a.toInt()) {
                public void actionPerformed(ActionEvent e) {
                    m_gb.humanMove(action);
                }
            });
            // buttonToAdd.setEnabled(true);
            p.add(buttonToAdd);
        }
        return p;
    }

    public JPanel handHistoryPanel(StateObserverBlackJack so) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        if (so.getHandHistory().size() > 10) {
            so.getHandHistory().remove(so.getHandHistory().get(0));
        }
        for (String line : so.getHandHistory()) {
            p.add(new JLabel(line));
        }
        return p;
    }

    public JPanel getHandPanel(Hand h) {
        JPanel handPanel = new JPanel();
        handPanel.add(new JLabel("Hand: "));
        if (h != null) {
            for (Card c : h.getCards()) {
                handPanel.add(getCard(c));
            }
        }
        return handPanel;
    }

    public JPanel playerPanel(Player p) {
        JPanel playerPanel = new JPanel();
        playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));
        playerPanel.add(new JLabel(p.name));
        playerPanel.add(new JLabel("chips: " + p.getChips()));

        for (Hand h : p.getHands()) {
            JPanel handPanel = getHandPanel(h);
            if (h.equals(p.getActiveHand())) {
                handPanel.setBorder(BorderFactory.createLineBorder(new Color(224, 36, 70), 4));
            }
            playerPanel.add(handPanel);
        }
        return playerPanel;
    }

    public JPanel dealerPanel(Player dealer) {
        JPanel dealerPanel = new JPanel();
        dealerPanel.setLayout(new BoxLayout(dealerPanel, BoxLayout.Y_AXIS));
        dealerPanel.add(new JLabel("Dealer"));
        if (dealer.getActiveHand() != null) {
            dealerPanel.add(getHandPanel(dealer.getActiveHand()));
        }
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
                this.setSize(1500, 800);
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
