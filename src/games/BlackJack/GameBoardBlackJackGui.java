package games.BlackJack;

import java.awt.event.ComponentListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import java.awt.FlowLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;

import games.Arena;
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
        SpringLayout spr = new SpringLayout();
        window.setLayout(spr);

        dealerZone = new JPanel();
        playerZone = new JPanel();
        actionZone = new JPanel();

        spr.putConstraint(SpringLayout.EAST, dealerZone, 0, SpringLayout.EAST, window);
        spr.putConstraint(SpringLayout.WEST, actionZone, 0, SpringLayout.WEST, window);
        spr.putConstraint(SpringLayout.EAST, playerZone, 0, SpringLayout.WEST, dealerZone);
        spr.putConstraint(SpringLayout.WEST, playerZone, 0, SpringLayout.EAST, actionZone);
        dealerZone.setBackground(new Color(100, 1, 1));
        playerZone.setBackground(new Color(1, 100, 1));
        actionZone.setBackground(new Color(1, 1, 100));

        window.add(actionZone);
        window.add(playerZone);
        window.add(dealerZone);

        spr.putConstraint(SpringLayout.NORTH, playerZone, 0, SpringLayout.NORTH, window);
        spr.putConstraint(SpringLayout.NORTH, dealerZone, 0, SpringLayout.NORTH, window);

        playerZone.setPreferredSize(new Dimension(450, 800));
        actionZone.setPreferredSize(new Dimension(600, 800));
        dealerZone.setPreferredSize(new Dimension(450, 800));

        m_so = (StateObserverBlackJack) m_gb.getStateObs();
        actionZone.setLayout(new GridLayout(2, 1));
        playerZone.setLayout(new BoxLayout(playerZone, BoxLayout.Y_AXIS));

        this.add(window);
        this.setVisible(true);
        this.pack();
        this.revalidate();
        this.repaint();
        this.addComponentListener(this);

    }

    public void update(StateObserverBlackJack so, boolean withReset, boolean showValueOnGameboard) {
        clear();

        m_so = so;
        actionZone.add(getActionZone(so));
        for (Player p : so.getPlayers()) {
            playerZone.add(playerPanel(p));
        }
        dealerZone.add(dealerPanel(so.getDealer()));
        actionZone.add(handHistoryPanel(so));
        this.revalidate();
        this.repaint();

    }

    public void updateWithSleep(StateObserverBlackJack so, int seconds) {
        update(so, false, false);
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        dealerZone.removeAll();
        playerZone.removeAll();
        actionZone.removeAll();
    }

    public JLabel getCard(Card c) {
        JLabel cardLabel = new JLabel(c.toString());
        cardLabel.setFont(cardLabel.getFont().deriveFont((float) 36.0));
        cardLabel.setPreferredSize(new Dimension(60, 80));
        cardLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardLabel.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0), 1));
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
        while (so.getHandHistory().size() > 17) {
            so.getHandHistory().remove(so.getHandHistory().get(0));
        }
        for (String line : so.getHandHistory()) {
            p.add(new JLabel(line));
        }
        return p;
    }

    public JPanel getHandPanel(Hand h) {
        JPanel handPanel = new JPanel();
        handPanel.setLayout(new FlowLayout());
        handPanel.add(new JLabel("Hand: "));
        if (h != null) {
            for (Card c : h.getCards()) {
                handPanel.add(getCard(c));
            }
        }
        handPanel.add(new JLabel(" Value: " + h.getHandValue()));
        return handPanel;
    }

    public JPanel playerPanel(Player p) {
        JPanel playerPanel = new JPanel();

        playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));
        playerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerPanel.add(createLabel(p.name + " with chips: " + p.getChips()));

        if (p.equals(m_so.getCurrentPlayer()) && !m_so.dealersTurn()) {
            playerPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 250, 0), 7));
        } else {
            playerPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0), 1));
        }
        JPanel handPanel = new JPanel();
        // playerPanel.add(handPanel);
        for (Hand h : p.getHands()) {
            handPanel = getHandPanel(h);
            if (h.equals(p.getActiveHand()) && p.equals(m_so.getCurrentPlayer()) && !m_so.dealersTurn()) {
                handPanel.setBorder(BorderFactory.createLineBorder(new Color(224, 36, 70), 2));
            }
            playerPanel.add(handPanel);
        }

        return playerPanel;
    }

    public JPanel dealerPanel(Dealer dealer) {
        JPanel dealerPanel = new JPanel();
        dealerPanel.setLayout(new BoxLayout(dealerPanel, BoxLayout.Y_AXIS));
        dealerPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        if (m_so.dealersTurn()) {
            dealerPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 250, 0), 4));
        }
        dealerPanel.add(createLabel("Dealer"));
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

    public JLabel createLabel(String content) {
        JLabel result = new JLabel(content);
        result.setAlignmentX(Component.CENTER_ALIGNMENT);
        result.setPreferredSize(new Dimension(400, 35));
        result.setFont(result.getFont().deriveFont((float) 16.0));
        return result;
    }

    public void toFront() {
        super.setState(JFrame.NORMAL); // if window is iconified, display it normally
        super.toFront();
    }

    public void destroy() {
        this.setVisible(false);
        this.dispose();
    }

    public void addComponentListener(JFrame f) {
        this.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                playerZone.setPreferredSize(new Dimension(450, f.getHeight()));
                actionZone.setPreferredSize(new Dimension(600, f.getHeight()));
                dealerZone.setPreferredSize(new Dimension(450, f.getHeight()));
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }

        });
    }

}
