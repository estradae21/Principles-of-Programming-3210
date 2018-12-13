/*  a Node holds one node of a parse tree
    with several pointers to children used
    depending on the kind of node
*/

import java.awt.*;
import java.util.Scanner;
import java.util.Stack;

public class Node {

  public static int count = 0;  // maintain unique id for each node
  public static double rv;
  private int id;

  private static Node fNode, aNode, pNode;
  private static boolean rb; //return boolean

  private String kind;  // non-terminal or terminal category for the node
  private String info;  // extra information about the node such as
                        // the actual identifier for an I

  // references to children in the parse tree
  private Node first, second, third;

  // memory table shared by all nodes
  private static Stack<MemTable> nTable;

  private static Scanner keys = new Scanner( System.in );

  // construct a common node with no info specified
  public Node( String k, Node one, Node two, Node three ) {
    kind = k;  info = "";
    first = one;  second = two;  third = three;
    id = count;
    count++;
    System.out.println( this );
  }

  // construct a node with specified info
  public Node( String k, String inf, Node one, Node two, Node three ) {
    kind = k;  info = inf;
    first = one;  second = two;  third = three;
    id = count;
    count++;
    System.out.println( this );
  }

  // construct a node that is essentially a token
  public Node( Token token ) {
    kind = token.getKind();  info = token.getDetails();
    first = null;  second = null;  third = null;
    id = count;
    count++;
    System.out.println( this );
  }

  public String toString() {
    return "#" + id + "[" + kind + "," + info + "]<" + nice(first) +
              " " + nice(second) + ">";
  }

  public String nice( Node node ) {
     if ( node == null ) {
        return "";
     }
     else {
        return "" + node.id;
     }
  }

  // produce array with the non-null children
  // in order
  private Node[] getChildren() {
    int count = 0;
    if( first != null ) count++;
    if( second != null ) count++;
    if( third != null ) count++;
    Node[] children = new Node[count];
    int k=0;
    if( first != null ) {  children[k] = first; k++; }
    if( second != null ) {  children[k] = second; k++; }
    if( third != null ) {  children[k] = third; k++; }

     return children;
  }

  //******************************************************
  // graphical display of this node and its subtree
  // in given camera, with specified location (x,y) of this
  // node, and specified distances horizontally and vertically
  // to children
  public void draw( Camera cam, double x, double y, double h, double v ) {

System.out.println("draw node " + id );

    // set drawing color
    cam.setColor( Color.black );

    String text = kind;
    if( ! info.equals("") ) text += "(" + info + ")";
    cam.drawHorizCenteredText( text, x, y );

    // positioning of children depends on how many
    // in a nice, uniform manner
    Node[] children = getChildren();
    int number = children.length;
System.out.println("has " + number + " children");

    double top = y - 0.75*v;

    if( number == 0 ) {
      return;
    }
    else if( number == 1 ) {
      children[0].draw( cam, x, y-v, h/2, v );     cam.drawLine( x, y, x, top );
    }
    else if( number == 2 ) {
      children[0].draw( cam, x-h/2, y-v, h/2, v );     cam.drawLine( x, y, x-h/2, top );
      children[1].draw( cam, x+h/2, y-v, h/2, v );     cam.drawLine( x, y, x+h/2, top );
    }
    else if( number == 3 ) {
      children[0].draw( cam, x-h, y-v, h/2, v );     cam.drawLine( x, y, x-h, top );
      children[1].draw( cam, x, y-v, h/2, v );     cam.drawLine( x, y, x, top );
      children[2].draw( cam, x+h, y-v, h/2, v );     cam.drawLine( x, y, x+h, top );
    }
    else {
      System.out.println("no Node kind has more than 3 children???");
      System.exit(1);
    }

  }// draw

  public static void error( String message ) {
    System.out.println( message );
    System.exit(1);
  }

  // ask this node to execute itself
  // (for nodes that don't return a value)
   public void execute() {

       if (kind.equals("prgrm")) {
           nTable = new Stack<MemTable>();
           if (second != null)
               fNode = second;
           else
               error("Program does have any function deffinitions");
           if (first != null)
               first.evaluate();
           else
               error("Program does not have an initial function call");
       }

       else if (kind.equals("fDef")) {
           MemTable table = nTable.pop();
           pNode = first;
           while(aNode != null && pNode != null){
               table.store(pNode.info, aNode.first.evaluate());
               if(pNode != null)
                   pNode = pNode.first;
               if(aNode != null)
                   aNode = aNode.second;
           }
           if(aNode != null || pNode != null)
               error("The number of arguments passed to function [" + info + "] does not match the number of parameters");
           nTable.push(table);
           if(second != null)
               second.execute();
       }

      else if (kind.equals("return")) {
          rv = first.evaluate();
          rb = true;
      }

      else if (kind.equals("if")) {
          if(first.evaluate() != 0) {
              if (second != null) {
                  second.execute();
              }
          }
          else {
              if (third != null) {
                  third.execute();
              }
          }
      }

      else if (kind.equals("stmts")) {
          if(first != null && first.kind.equals("fCall")) {
              first.evaluate();
          }
          else if (first != null) {
              first.execute();
          }
          if (second != null && !rb) {
              second.execute();
          }
      }

      else if ( kind.equals("prtstr") ) {
         System.out.print( info );
      }

      else if ( kind.equals("print") ) {
          double value = first.evaluate();
          if (value % 1 == 0) {
              System.out.print((int)value);
          } else {
              System.out.print(value);
          }
      }

      else if ( kind.equals("nl") ) {
         System.out.print( "\n" );
      }

      else if ( kind.equals("sto") ) {
          double val = first.evaluate();
          MemTable table = nTable.pop();
          table.store(info,val);
          nTable.push(table);
      }

      else {
         error("Unknown kind of node [" + kind + "]");
      }

   }// execute

   // compute and return value produced by this node
   public double evaluate() {

      if (kind.equals("fCall")) {
          boolean located = false;
          boolean eof = false;
          nTable.push(new MemTable());
          aNode = first;
          Node node = fNode;
          while (!located && !eof) {
              if (info.equals(node.first.info)) {
                  located = true;
                  node.first.execute();
              }
              else {
                  if (node.second != null) {
                      node = node.second;
                  }
                  else {
                      eof = true;
                  }
              }
          }
          nTable.pop();
          rb = false;
          return rv;
      }

      else if ( kind.equals("num") ) {
         return Double.parseDouble( info );
      }

      else if ( kind.equals("var") ) {
          MemTable table = nTable.pop();
          double value = table.retrieve(info);
          nTable.push(table);
          return value;
      }

      else if ( kind.equals("+") || kind.equals("-") ) {
          double val1 = first.evaluate();
          double val2 = second.evaluate();
          if (kind.equals("+"))
              return val1 + val2;
          else
              return val1 - val2;
      }

      else if ( kind.equals("*") || kind.equals("/") ) {
          double val1 = first.evaluate();
          double val2 = second.evaluate();
          if (kind.equals("*"))
              return val1 * val2;
          else
              return val1 / val2;
       }
     
      else if ( kind.equals("input") ) {
          return keys.nextDouble();
      }

      else if ( kind.equals("sqrt") || kind.equals("cos") ||
                kind.equals("sin") || kind.equals("atan")) {
          double value = first.evaluate();

          if ( kind.equals("sqrt") )
             return Math.sqrt(value);
          else if ( kind.equals("cos") )
             return Math.cos( Math.toRadians( value ) );
          else if ( kind.equals("sin") )
             return Math.sin( Math.toRadians( value ) );
          else if ( kind.equals("atan") )
             return Math.toDegrees( Math.atan( value ) );
          else {
             error("unknown function name [" + kind + "]");
             return 0;
          }
       }

      else if ( kind.equals("pow") ) {
           double val1 = first.evaluate();
           double val2 = second.evaluate();
           return Math.pow(val1, val2);
      }

      else if (kind.equals("round")) {
          return Math.round(first.evaluate());
      }

      else if (kind.equals("trunc")) {
          return Math.floor(first.evaluate());
      }

      else if ( kind.equals("opp") ) {
           double val = first.evaluate();
           return -val;
      }

      else if (kind.equals("lt")) {
           double val1 = first.evaluate();
           double val2 = second.evaluate();
           if (val1 < val2) return 1;
           else return 0;
      }

      else if (kind.equals("le")) {
          double val1 = first.evaluate();
          double val2 = second.evaluate();
          if (val1 <= val2) return 1;
          else return 0;
      }

      else if (kind.equals("eq")) {
          double val1 = first.evaluate();
          double val2 = second.evaluate();
          if (val1 == val2) return 1;
          else return 0;
      }

      else if (kind.equals("ne")) {
          double val1 = first.evaluate();
          double val2 = second.evaluate();
          if (val1 != val2) return 1;
          else return 0;
      }

      else if (kind.equals("or")) {
          double val1 = first.evaluate();
          double val2 = second.evaluate();
          if (val1 != 0 || val2 != 0) {
              return 1;
          }
          else return 0;
      }

      else if (kind.equals("and")) {
          double val1 = first.evaluate();
          double val2 = second.evaluate();
          if (val1 != 0 && val2 !=0) {
              return 1;
          }
          else return 0;
      }

      else if (kind.equals("not")) {
          double val1 = first.evaluate();
          if (val1 == 0) {
              return 1;
          }
          else return 0;
      }

       else {
          error("Unknown node kind [" + kind + "]");
          return 0;
       }

   }
   // evaluate

}// Node
