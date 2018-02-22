package org.batfish.z3.state;

import org.batfish.z3.expr.BasicStateExpr;
import org.batfish.z3.state.visitors.StateExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

public class NodeDropNullRoute extends BasicStateExpr {

  public static class State extends BasicStateExpr.State {

    public static final State INSTANCE = new State();

    private State() {}

    @Override
    public void accept(StateVisitor visitor) {
      visitor.visitNodeDropNullRoute(this);
    }
  }

  private final String _hostname;

  public NodeDropNullRoute(String hostname) {
    _hostname = hostname;
  }

  @Override
  public void accept(StateExprVisitor visitor) {
    visitor.visitNodeDropNullRoute(this);
  }

  public String getHostname() {
    return _hostname;
  }

  @Override
  public State getState() {
    return State.INSTANCE;
  }
}