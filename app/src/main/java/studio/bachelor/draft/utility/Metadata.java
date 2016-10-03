package studio.bachelor.draft.utility;


import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Created by BACHELOR on 2016/03/16.
 */
public interface Metadata {
    Node transformStateToDOMNode(Document document);
    String getElementName();
}
