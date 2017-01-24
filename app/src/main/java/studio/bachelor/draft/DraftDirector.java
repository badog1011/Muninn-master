package studio.bachelor.draft;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import studio.bachelor.draft.marker.AnchorMarker;
import studio.bachelor.draft.marker.ControlMarker;
import studio.bachelor.draft.marker.LabelMarker;
import studio.bachelor.draft.marker.LinkMarker;
import studio.bachelor.draft.marker.Marker;
import studio.bachelor.draft.marker.MeasureMarker;
import studio.bachelor.draft.marker.builder.ControlMarkerBuilder;
import studio.bachelor.draft.marker.builder.LabelMarkerBuilder;
import studio.bachelor.draft.marker.builder.LinkMarkerBuilder;
import studio.bachelor.draft.marker.builder.MeasureMarkerBuilder;
import studio.bachelor.draft.toolbox.Toolbox;
import studio.bachelor.draft.utility.BitmapMD5Encoder;
import studio.bachelor.draft.utility.DataStepByStep;
import studio.bachelor.draft.utility.MapString;
import studio.bachelor.draft.utility.Position;
import studio.bachelor.draft.utility.Renderable;
import studio.bachelor.draft.utility.Selectable;
import studio.bachelor.draft.utility.SignPad;
import studio.bachelor.draft.utility.renderer.DraftRenderer;
import studio.bachelor.draft.utility.renderer.RendererManager;
import studio.bachelor.draft.utility.renderer.ToolboxRenderer;
import studio.bachelor.draft.utility.renderer.builder.MarkerRendererBuilder;
import studio.bachelor.muninn.Muninn;
import studio.bachelor.utility.FTPUploader;

/**
 * Created by BACHELOR on 2016/02/24.
 */
public class DraftDirector {
    private final String TAG = "DraftDirector";
    public static final DraftDirector instance = new DraftDirector();
    private Draft draft;
    private DraftRenderer draftRenderer;
    private RendererManager rendererManager;
    private Map<Object, Renderable> renderableMap = new HashMap<Object, Renderable>();
    public LinkedList<Marker> RedoTempLL = new LinkedList<Marker>();
    public static LinkedList<DataStepByStep> StepByStepUndo = new LinkedList<DataStepByStep>();
    public static LinkedList<DataStepByStep> StepByStepRedo = new LinkedList<DataStepByStep>();
    private final Toolbox toolbox = Toolbox.getInstance();
    private ToolboxRenderer toolboxRenderer;
    private Type markerType = MeasureMarker.class;
    private Marker markerHold;
    private Marker markerSelecting;
    private Marker markerSelected;
    private Toolbox.Tool tool;
    private final Paint paint = new Paint();
    //private final Paint pathPaint = new Paint();
    private Context context;
    private int nextObjectID = 0;
    //private Uri birdViewUri;
    private Bitmap birdview;
    private BitmapMD5Encoder MD5Encoder;
    private Thread MD5EncoderThread;
    private List<File> signFiles = new LinkedList<File>();

    boolean firstTime = true; //Create後，firstTime = false; Delete後，firstTime = true


    {
        draft = Draft.getInstance();
        draftRenderer = new DraftRenderer(draft);
        rendererManager = RendererManager.getInstance();
    }

    private DraftDirector() {

    }

    public void setViewContext(Context context) {
        this.context = context;
    }

    public int allocateObjectID() {
        return nextObjectID++;
    }

    public void setBirdviewImageByUri(Uri uri) {
        signFiles.clear();
        if(MD5EncoderThread != null)
            MD5EncoderThread.interrupt();
        //birdViewUri = uri;
        try {
            birdview = MediaStore.Images.Media.getBitmap(Muninn.getContext().getContentResolver(), uri);
            MD5Encoder = new BitmapMD5Encoder(birdview); //建立Runnable類別，依據BitMap圖檔編碼MD5
            MD5EncoderThread = new Thread(MD5Encoder); //建立Thread
            MD5EncoderThread.start();
        } catch (Exception e) {
            Log.d("DraftRenderer", "setBirdview(Uri uri)" + e.toString());
        }
        draftRenderer.setBirdview(birdview); //設定選取好的圖片
        draft.setWidth(birdview.getWidth()); //setting the width-size of draft according to the birdview.
        draft.setHeight(birdview.getHeight());
    }

    public void setWidthAndHeight(float width, float height) {
        this.draft.setWidthAndHeight(width, height);
    }

    public void setToolboxRenderer(Position upper_left_corner, float width, float height) {
        toolboxRenderer = new ToolboxRenderer(toolbox, upper_left_corner, width, height);
    }

    public void createPathIfPathMode(Position position) { //curve
        if (tool == Toolbox.Tool.PATH_MODE) {
            draft.createPathIfPathMode(position);
        }
    }

    public void recordPath(Position position) {
        if (tool == Toolbox.Tool.PATH_MODE) {
            draft.recordPath(position);
        }
    }

    public void endPath(Position position) {
        if (tool == Toolbox.Tool.PATH_MODE) {
            draft.endPath(position);
        }
    }

    private Position prevLayerPosition = new Position();
    private boolean enableMoving = false;

    public void moveLayerCreate(Position p) {
        if (getTool() != Toolbox.Tool.PATH_MODE && enableMoving == false) {
            enableMoving = true;
            prevLayerPosition.set(p);
        }

    }

    public void moveLayerStart(Position p) {
        if (enableMoving && markerHold == null) {
            double x = p.x - prevLayerPosition.x;
            double y = p.y - prevLayerPosition.y;
            Position offset = new Position(x, y);
            moveDraft(offset);
            prevLayerPosition.set(p);
        }

    }

    public void moveLayerStop() {
            enableMoving = false;
    }

    public void addMarker(Position position) {
        Muninn.sound_Ding.seekTo(0);
        Muninn.sound_Ding.start();
        if (markerType == MeasureMarker.class) {
            addMeasureMarker(position);
        } else if (markerType == AnchorMarker.class) {
            if (firstTime) {
                firstTime = false; //global var
                addAnchorMarker(position, true);
            } else {
                addAnchorMarker(position, false); //已存在一個AnchorMarker，重新新增的
            }

        } else if (markerType == LabelMarker.class) {
            addLabelMarker(position);
        }
    }

    private void addLabelMarker(Position position) {
        LabelMarkerBuilder lb = new LabelMarkerBuilder();
        final Marker marker = lb.
                setPosition(new Position(position.x, position.y)).
                build();

        final EditText edit_text = new EditText(context);

        AlertDialog.Builder dialog_builder = new AlertDialog.Builder(context);
        dialog_builder
                .setTitle("標籤資訊")
                .setView(edit_text)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String label_str = edit_text.getText().toString();
                        if (label_str.isEmpty())
                            return;
                        ((LabelMarker) marker).setLabel(label_str);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((LabelMarker) marker).remove();
                    }

                })
                .show();

        draft.addMarker(marker);

        marker.refreshed_Layer_position.set(marker.position);
        marker.historyLayerPositionsUndo.add(new Position(marker.refreshed_Layer_position.x, marker.refreshed_Layer_position.y)); //After getting Layer-position
        Log.d(TAG, "marker.position = (" + marker.position.x + ", " + marker.position.y + ")");
        Log.d(TAG, "marker.refreshed_Layer_position = (" + marker.refreshed_Layer_position.x + ", " + marker.refreshed_Layer_position.y + ")");
        Log.d(TAG, "historyLayerPositionsUndo = (" + marker.historyLayerPositionsUndo.getLast().x + ", " + marker.historyLayerPositionsUndo.getLast().y + ")");

        //  建立MakerRenderer
        MarkerRendererBuilder mrb = new MarkerRendererBuilder();
        Renderable marker_renderer = mrb.
                setReference(marker).
                setPoint(marker).
                setText(new MapString((LabelMarker) marker), marker.position).
                build();

        rendererManager.addRenderer(marker_renderer);

        //  建立對應關係
        renderableMap.put(marker, marker_renderer);

        StepByStepUndo.add(new DataStepByStep(marker, Selectable.CRUD.CREATE));
    }


    //2016/10/17 By Jonas
    private void updateLabelMarker(DataStepByStep data) {
        Marker tMarker = data.getMarker();

        if (renderableMap.containsKey(tMarker)) {
            rendererManager.removeRenderer(renderableMap.get(tMarker));
        }

        tMarker.position.set(tMarker.refreshed_Layer_position);

        ((LabelMarker) tMarker).setLabel(((LabelMarker) tMarker).getLabel());

        draft.addMarkerLayerPosition(tMarker);

        //  建立MakerRenderer
        MarkerRendererBuilder mrb = new MarkerRendererBuilder();
        Renderable marker_renderer = mrb.
                setReference(tMarker).
                setPoint(tMarker).
                setText(new MapString((LabelMarker) tMarker), tMarker.position).
                build();

        rendererManager.addRenderer(marker_renderer);

        //  建立對應關係
        renderableMap.put(tMarker, marker_renderer);

    }

    private void addAnchorMarker(Position position, Boolean firstTime) {
        //  取得AnchorMarker與ControlMaker
        final Marker marker = AnchorMarker.getInstance();
        Marker linked = AnchorMarker.getInstance().getLink(); //this link was created by marker.

        if (renderableMap.containsKey(marker) && renderableMap.containsKey(linked)) {
            rendererManager.removeRenderer(renderableMap.get(marker));
            rendererManager.removeRenderer(renderableMap.get(linked));
        }

        final EditText edit_text = new EditText(context);
        edit_text.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        AlertDialog.Builder dialog_builder = new AlertDialog.Builder(context);
        dialog_builder
                .setTitle("真實距離")
                .setView(edit_text)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String distance_str = edit_text.getText().toString();
                        Log.d(TAG, "distance string=======================================> " + distance_str);
                        if (distance_str.isEmpty())
                            return;

                        ((AnchorMarker) marker).setRealDistance(Double.parseDouble(distance_str));
                        AnchorMarker.historyDistancesUndo.addLast( Double.parseDouble(distance_str) ); //新增第一個distance至history
                    }
                })
                .show();

        marker.position.set(position);
        linked.position.set(new Position(position.x + 50, position.y + 50)); //linked位移(x, y) = (50, 50)
        marker.refreshed_tap_position.set(position);
        linked.refreshed_tap_position.set(new Position(position.x + 50, position.y + 50)); //linked位移(x, y) = (50, 50)
        ((ControlMarker)linked).setMarker(marker); //tell linked who is his daddy, marker

        draft.addMarker(marker); //add to markerList by MarkerManager
        draft.addMarker(linked); //add to markerList by MarkerManager

        marker.refreshed_Layer_position.set(marker.position);
        marker.historyLayerPositionsUndo.add(new Position(marker.refreshed_Layer_position.x, marker.refreshed_Layer_position.y)); //After getting Layer-position
        linked.refreshed_Layer_position.set(linked.position);
        linked.historyLayerPositionsUndo.add(new Position(linked.refreshed_Layer_position.x, linked.refreshed_Layer_position.y)); //After getting Layer-position

        Position[] positions = {marker.position, linked.position};
        List<Position> position_list = new ArrayList<Position>(Arrays.asList(positions));

        //  建立MakerRenderer
        MarkerRendererBuilder mrb = new MarkerRendererBuilder();
        Renderable marker_renderer = mrb.
                setLinkLine((LinkMarker) marker). //set head and tail
                setReference(marker). //參考marker
                setPoint(marker).
                setText(new MapString((AnchorMarker) marker), position_list).
                build();

        Renderable link_renderer = mrb.
                setReference(linked).
                build();

        rendererManager.addRenderer(marker_renderer);
        rendererManager.addRenderer(link_renderer);

        //  建立對應關係
        renderableMap.put(marker, marker_renderer);
        renderableMap.put(linked, link_renderer);

        //第一次為Create，之後的皆視為Update
        if (firstTime) {
            StepByStepUndo.add(new DataStepByStep(marker, Selectable.CRUD.CREATE));
        } else {
            StepByStepUndo.add(new DataStepByStep(marker, Selectable.CRUD.UPDATE));
        }

    }

    //更新Anchor點的位置
    private void updateAnchorMarker() {
        //  取得AnchorMarker與ControlMaker
        final Marker marker = AnchorMarker.getInstance();
        Marker linked = AnchorMarker.getInstance().getLink(); //this link was created by marker.

        if (renderableMap.containsKey(marker) && renderableMap.containsKey(linked)) {
            rendererManager.removeRenderer(renderableMap.get(marker));
            rendererManager.removeRenderer(renderableMap.get(linked));
        }


        ((AnchorMarker) marker).setRealDistance( ((AnchorMarker)marker).getRealDistance() );

        marker.position.set(marker.refreshed_Layer_position);
        linked.position.set(linked.refreshed_Layer_position);

        draft.addMarkerLayerPosition(marker); //add to markerList by MarkerManager
        draft.addMarkerLayerPosition(linked); //add to markerList by MarkerManager

        Position[] positions = {marker.position, linked.position};
        List<Position> position_list = new ArrayList<Position>(Arrays.asList(positions));

        //  建立MakerRenderer
        MarkerRendererBuilder mrb = new MarkerRendererBuilder();
        Renderable marker_renderer = mrb.
                setLinkLine((LinkMarker) marker). //set head and tail
                setReference(marker). //參考marker
                setPoint(marker).
                setText(new MapString((AnchorMarker) marker), position_list).
                build();

        Renderable link_renderer = mrb.
                setReference(linked).
                build();

        rendererManager.addRenderer(marker_renderer);
        rendererManager.addRenderer(link_renderer);

        //  建立對應關係
        renderableMap.put(marker, marker_renderer);
        renderableMap.put(linked, link_renderer);

    }

    public void removeMarker(Marker marker) {
        Log.d(TAG, "removeMarker(Marker marker)");
        if (marker == null) //abstract "Marker" will call one time.
            return;
        if (renderableMap.containsKey(marker)) { //檢查Map是否有此marker，有則刪除
            Log.d(TAG, "renderableMap contain!!");
            Renderable renderable = renderableMap.get(marker); //取得此marker的renderable
            rendererManager.removeRenderer(renderable); //刪除renderObjects裡的render_object
            renderableMap.remove(marker);
        }
        draft.removeMarker(marker);
    }

    /*
        (linked)*---------*(marker)
     */

    private void addMeasureMarker(Position position) {
        //  Step1.1:建立 ControlMarkerBuilder 與Step1.2:建立 LinkMarkerBuilder
        ControlMarkerBuilder cb = new ControlMarkerBuilder();
        Marker linked = cb.
                setPosition(new Position(position.x - 100, position.y)).
                build(); //return Marker
        LinkMarkerBuilder lb = new MeasureMarkerBuilder();
        Marker marker = lb.
                setPosition(position).
                setLink(linked). //儲存linked marker，並且告知linked誰是他老爸marker
                build(); //return Marker

        ((ControlMarker)linked).setMarker(marker); //tell linked who is his daddy

        Log.d(TAG, "linked: (" + linked.refreshed_tap_position.x + ", " + linked.refreshed_tap_position.y + ") marker: (" + marker.refreshed_tap_position.x + ", " + marker.refreshed_tap_position.y + ")");

        draft.addMarker(marker);//this will be adjusted the position of marker in draft
        draft.addMarker(linked);//this will be adjusted the position of marker in draft

        marker.refreshed_Layer_position.set(marker.position);
        marker.historyLayerPositionsUndo.add(new Position(marker.refreshed_Layer_position.x, marker.refreshed_Layer_position.y)); //After getting Layer-position
        linked.refreshed_Layer_position.set(linked.position);
        linked.historyLayerPositionsUndo.add(new Position(linked.refreshed_Layer_position.x, linked.refreshed_Layer_position.y)); //After getting Layer-position


        Position[] positions = {marker.position, linked.position};
        List<Position> position_list = new ArrayList<Position>(Arrays.asList(positions));

        //  Step2: 建立MakerRenderer
        MarkerRendererBuilder mrb = new MarkerRendererBuilder();
        Renderable marker_renderer = mrb.
                setLinkLine((LinkMarker) marker).
                setReference(marker).
                setPoint(marker).
                setText(new MapString((MeasureMarker) marker), position_list).
                build(); //product will be cleared

        Renderable link_renderer = mrb.
                setReference(linked). //create the relationship between marker and render
                build(); //product will be cleared

        rendererManager.addRenderer(marker_renderer);
        rendererManager.addRenderer(link_renderer);

        //  建立對應關係
        renderableMap.put(marker, marker_renderer);
        renderableMap.put(linked, link_renderer);

        StepByStepUndo.add(new DataStepByStep(marker, Selectable.CRUD.CREATE));
    }


    private void updateMeasureMarker(DataStepByStep data) {//ISSUE:因改為更新舊有資料，並非新增一個新的MeasureMarker
        Marker marker = data.getMarker();
        Marker linker = ((LinkMarker)marker).getLink();

        if (renderableMap.containsKey(marker) && renderableMap.containsKey(linker)) {
            rendererManager.removeRenderer(renderableMap.get(marker));
            rendererManager.removeRenderer(renderableMap.get(linker));
        }

        marker.position.set(marker.refreshed_Layer_position);
        linker.position.set(linker.refreshed_Layer_position);

        draft.addMarkerLayerPosition(marker); //會取得Layer上的位置
        draft.addMarkerLayerPosition(linker); //會取得Layer上的位置

        Position[] positions = {marker.position, linker.position};
        List<Position> position_list = new ArrayList<Position>(Arrays.asList(positions));

        //  建立MakerRenderer
        MarkerRendererBuilder mrb = new MarkerRendererBuilder();
        Renderable marker_renderer = mrb.
                setLinkLine((LinkMarker) marker).
                setReference(marker).
                setPoint(marker).
                setText(new MapString((MeasureMarker) marker), position_list).
                build();

        Renderable link_renderer = mrb.
                setReference(linker).
                build();

        rendererManager.addRenderer(marker_renderer);
        rendererManager.addRenderer(link_renderer);

        //  建立對應關係
        renderableMap.put(marker, marker_renderer);
        renderableMap.put(linker, link_renderer);

    }


    public Marker getNearestMarker(Position position) {
        return draft.getNearestMarker(position); //點選時，取得最接近的Marker原件
    }

    public Toolbox.Tool getNearestTool(Position position) {
        return toolboxRenderer.getInstance(position, 64); //點選時，取得最接近的tool原件
    }

    public void render(Canvas canvas) {
        canvas.save();
        draftRenderer.onDraw(canvas);

        for (Renderable renderable : rendererManager.renderObjects) { //畫上所有物件e.g Line, Anchor, Label and etc.
            renderable.onDraw(canvas);
        }

        canvas.restore();

        if (toolboxRenderer != null)
            toolboxRenderer.onDraw(canvas);

        if (tool != null) {
            Bitmap bitmap = ToolboxRenderer.getToolIcon(tool); //依據tool(key)取得icon resource(value)
            canvas.drawBitmap(bitmap, canvas.getWidth() - bitmap.getWidth(), canvas.getHeight() - bitmap.getHeight(), paint); //將icon放置右下角
        }
    }

    public void selectTool(Toolbox.Tool tool) {
        if (tool == Toolbox.Tool.CLEAR_PATH) {
            Muninn.sound_Punch.seekTo(0);
            Muninn.sound_Punch.start();
            draft.clearPaths(); //清除草稿線(PATH_MODE)
        }
        else {
            Muninn.sound_Ding.seekTo(0); //重至0毫秒
            Muninn.sound_Ding.start();
            this.tool = tool; //assigned selected component
        }

        switch (tool) {
            case MAKER_TYPE_LINK:
                this.markerType = MeasureMarker.class;
                break;
            case MAKER_TYPE_ANCHOR:
                this.markerType = AnchorMarker.class;
                break;
            case MARKER_TYPE_LABEL:
                this.markerType = LabelMarker.class;
                break;
            case EDIT_UNDO:
                doUndoTask();
                break;
            case EDIT_REDO:
                doRedoTask();
                break;
        }
    }


    private void doUndoTask() {

        Log.d(TAG, "EDIT_UNDO2====================================");
        if (!StepByStepUndo.isEmpty()) { //不為空

            DataStepByStep data = StepByStepUndo.pollLast();
            Marker dataMarker = data.getMarker();

            switch (data.getCRUDstate()) {
                case CREATE:
                    //do delete
                    StepByStepRedo.add(new DataStepByStep(dataMarker, Selectable.CRUD.DELETE));
                    if (dataMarker instanceof LabelMarker) {
                        Log.d(TAG, "LabelMarker");
                        this.removeMarker(dataMarker);
                    } else if (dataMarker instanceof MeasureMarker){ //MeasureMarker, AnchorMarker
                        this.removeMarker( ((LinkMarker)dataMarker).getLink() );
                        this.removeMarker( dataMarker );
                    } else if (dataMarker instanceof AnchorMarker) {
                        firstTime = true;
                        this.removeMarker( ((LinkMarker)dataMarker).getLink() );
                        this.removeMarker( dataMarker );
                    }

                    break;
                case UPDATE:
                    //Looking up old data
                    if (dataMarker instanceof LabelMarker) {
                        Log.d(TAG, "Undo Update: LabelMarker");
                        Log.d(TAG, "historyLayerPositionsUndo.size(): " + dataMarker.historyLayerPositionsUndo.size());
                        if (dataMarker.historyLayerPositionsUndo.size() > 1) { //第一個是原始位置
                            StepByStepRedo.add(new DataStepByStep(dataMarker, Selectable.CRUD.UPDATE));
                            this.removeMarker(dataMarker); //刪掉最新的位置renderer，還原前一位置

                            Position redoPositionRef = dataMarker.historyLayerPositionsUndo.pollLast(); //give this to redoLL
                            Position redoPosition = new Position(redoPositionRef.x, redoPositionRef.y);
                            dataMarker.historyLayerPositionsRedo.addLast(redoPosition); //!!important!! new one Position
                            dataMarker.refreshed_Layer_position.set(dataMarker.historyLayerPositionsUndo.getLast()); //update tap-position
                            this.updateLabelMarker(data);
                        }

                    } else if (dataMarker instanceof MeasureMarker) { //MeasureMarker
                        Log.d(TAG, "Undo Update: MeasureMarker");
                        StepByStepRedo.add(new DataStepByStep(dataMarker, Selectable.CRUD.UPDATE));
                        this.removeMarker( ((LinkMarker)dataMarker).getLink() );
                        this.removeMarker( dataMarker );

                        Position redoPositionRef = dataMarker.historyLayerPositionsUndo.pollLast(); //give this to redoLL
                        Position redoPosition = new Position(redoPositionRef.x, redoPositionRef.y);
                        dataMarker.historyLayerPositionsRedo.addLast(redoPosition); //!!important!! new one Position
                        dataMarker.refreshed_Layer_position.set(dataMarker.historyLayerPositionsUndo.getLast()); //update tap-position

                        //Linked (ControlMarker)
                        Position redoPositionRefLink = ((LinkMarker) dataMarker).getLink().historyLayerPositionsUndo.pollLast(); //give this to redoLL
                        Position redoPositionLink = new Position(redoPositionRefLink.x, redoPositionRefLink.y);
                        ((LinkMarker) dataMarker).getLink().historyLayerPositionsRedo.addLast(redoPositionLink); //!!important!! new one Position
                        ((LinkMarker) dataMarker).getLink().refreshed_Layer_position.set( ((LinkMarker) dataMarker).getLink().historyLayerPositionsUndo.getLast() ); //update tap-position

                        this.updateMeasureMarker(data);

                    } else if (dataMarker instanceof AnchorMarker) { //AnchorMarker
                        Log.d(TAG, "Undo Update: MeasureMarker");
                        StepByStepRedo.add(new DataStepByStep(dataMarker, Selectable.CRUD.UPDATE));
                        this.removeMarker( ((LinkMarker)dataMarker).getLink() );
                        this.removeMarker( dataMarker );

                        Position redoPositionRef = dataMarker.historyLayerPositionsUndo.pollLast(); //give this to redoLL
                        Position redoPosition = new Position(redoPositionRef.x, redoPositionRef.y);
                        dataMarker.historyLayerPositionsRedo.addLast(redoPosition); //!!important!! new one Position
                        dataMarker.refreshed_Layer_position.set(dataMarker.historyLayerPositionsUndo.getLast()); //update tap-position

                        //Linked (ControlMarker)
                        Position redoPositionRefLink = ((LinkMarker) dataMarker).getLink().historyLayerPositionsUndo.pollLast(); //give this to redoLL
                        Position redoPositionLink = new Position(redoPositionRefLink.x, redoPositionRefLink.y);
                        ((LinkMarker) dataMarker).getLink().historyLayerPositionsRedo.addLast(redoPositionLink); //!!important!! new one Position
                        ((LinkMarker) dataMarker).getLink().refreshed_Layer_position.set( ((LinkMarker) dataMarker).getLink().historyLayerPositionsUndo.getLast() ); //update tap-position

                        //deal with Label
                        if (AnchorMarker.historyDistancesUndo.size() > 1) { //防呆避免underflow
                            double redoDistance = AnchorMarker.historyDistancesUndo.pollLast(); //抓取並刪除history最新distance
                            AnchorMarker.historyDistancesRedo.addLast(redoDistance);
                            ((AnchorMarker)dataMarker).setRealDistance(AnchorMarker.historyDistancesUndo.getLast()); //還原history裡上一個distance
                            this.updateAnchorMarker();
                        }

                    }
                    break;
                case DELETE:
                    //do re-create()
                    if ( dataMarker instanceof MeasureMarker) {
                        this.updateMeasureMarker(data);
                        StepByStepRedo.add(new DataStepByStep(dataMarker, Selectable.CRUD.CREATE));

                    } else if ( dataMarker instanceof AnchorMarker) {
                        this.updateAnchorMarker();
                        StepByStepRedo.add(new DataStepByStep(dataMarker, Selectable.CRUD.CREATE));

                    } else if ( dataMarker instanceof LabelMarker) {
                        this.updateLabelMarker(data);
                        StepByStepRedo.add(new DataStepByStep(dataMarker, Selectable.CRUD.CREATE));

                    }
                    break;
            }
        }



    }


    private void doRedoTask() {
        Log.d(TAG, "EDIT_REDO2====================================");

        if (!StepByStepRedo.isEmpty()) { //不為空

            DataStepByStep data = StepByStepRedo.pollLast();
            Marker dataMarker = data.getMarker();

            switch (data.getCRUDstate()) {
                case CREATE:
                    //do delete();
                    StepByStepUndo.add(new DataStepByStep(dataMarker, Selectable.CRUD.DELETE));
                    if (dataMarker instanceof LabelMarker) {
                        Log.d(TAG, "LabelMarker");
                        this.removeMarker(dataMarker);
                    } else if (dataMarker instanceof MeasureMarker){ //MeasureMarker, AnchorMarker
                        this.removeMarker( ((LinkMarker)dataMarker).getLink() );
                        this.removeMarker( dataMarker );
                    } else if (dataMarker instanceof AnchorMarker) {
                        firstTime = true;
                        this.removeMarker( ((LinkMarker)dataMarker).getLink() );
                        this.removeMarker( dataMarker );
                    }


                    break;
                case UPDATE:
                    //Looking up old data in every marker
                    if (dataMarker instanceof LabelMarker) {
                        Log.d(TAG, "Redo: Update: LabelMarker");
                        if (!dataMarker.historyLayerPositionsRedo.isEmpty()) {

                            this.removeMarker(dataMarker); //移除目前的marker，還原至前一位置

                            Position undoPositionRef = dataMarker.historyLayerPositionsRedo.pollLast();
                            Position undoPosition = new Position(undoPositionRef.x, undoPositionRef.y);
                            dataMarker.historyLayerPositionsUndo.addLast(undoPosition);
                            dataMarker.refreshed_Layer_position.set(undoPosition); //update tap-position
                            this.updateLabelMarker(data);
                            StepByStepUndo.add(new DataStepByStep(dataMarker, Selectable.CRUD.UPDATE));
                        } //if ()

                    } else if (dataMarker instanceof MeasureMarker){ //MeasureMarker
                        Log.d(TAG, "Redo: Update: MeasureMarker");
                        this.removeMarker( ((LinkMarker)dataMarker).getLink() );
                        this.removeMarker( dataMarker );

                        Position undoPositionRef = dataMarker.historyLayerPositionsRedo.pollLast(); //give this to redoLL
                        Position undoPosition = new Position(undoPositionRef.x, undoPositionRef.y);
                        dataMarker.historyLayerPositionsUndo.addLast(undoPosition); //!!important!! new one Position
                        dataMarker.refreshed_Layer_position.set(undoPosition); //update tap-position

                        //Linked (ControlMarker)
                        Position undoPositionRefLink = ((LinkMarker) dataMarker).getLink().historyLayerPositionsRedo.pollLast(); //give this to redoLL
                        Position undoPositionLink = new Position(undoPositionRefLink.x, undoPositionRefLink.y);
                        ((LinkMarker) dataMarker).getLink().historyLayerPositionsUndo.addLast(undoPositionLink); //!!important!! new one Position
                        ((LinkMarker) dataMarker).getLink().refreshed_Layer_position.set(undoPositionLink); //update tap-position

                        this.updateMeasureMarker(data);

                        StepByStepUndo.add(new DataStepByStep(dataMarker, Selectable.CRUD.UPDATE));

                    } else if (dataMarker instanceof AnchorMarker) { //AnchorMarker
                        Log.d(TAG, "Redo: Update: AnchorMarker");
                        this.removeMarker( ((LinkMarker)dataMarker).getLink() );
                        this.removeMarker( dataMarker );

                        Position redoPositionRef = dataMarker.historyLayerPositionsRedo.pollLast(); //give this to redoLL
                        Position redoPosition = new Position(redoPositionRef.x, redoPositionRef.y);
                        dataMarker.historyLayerPositionsUndo.addLast(redoPosition); //!!important!! new one Position
                        dataMarker.refreshed_Layer_position.set(redoPosition); //update tap-position

                        //Linked (ControlMarker)
                        Position redoPositionRefLink = ((LinkMarker) dataMarker).getLink().historyLayerPositionsRedo.pollLast(); //give this to redoLL
                        Position redoPositionLink = new Position(redoPositionRefLink.x, redoPositionRefLink.y);
                        ((LinkMarker) dataMarker).getLink().historyLayerPositionsUndo.addLast(redoPositionLink); //!!important!! new one Position
                        ((LinkMarker) dataMarker).getLink().refreshed_Layer_position.set(redoPositionLink ); //update tap-position

                        //deal with Label
                        double redoDistance = AnchorMarker.historyDistancesRedo.pollLast(); //抓取並刪除history最新distance
                        AnchorMarker.historyDistancesUndo.addLast(redoDistance);

                        ((AnchorMarker)dataMarker).setRealDistance(redoDistance); //還原history裡上一個distance

                        this.updateAnchorMarker();

                        StepByStepUndo.add(new DataStepByStep(dataMarker, Selectable.CRUD.UPDATE));


                    }
                    break;
                case DELETE:
                    //do Re-create()
                    if (dataMarker instanceof LabelMarker) {
                        Log.d(TAG, "REDO: updateLabelMarker()");
                        this.updateLabelMarker(data); //在Redo中使用update來create舊的marker，避免新增新的marker!!若新創marker，會造成後面update與這有關的marker刪除不了之。
                        StepByStepUndo.add(new DataStepByStep(dataMarker, Selectable.CRUD.CREATE));

                    } else if (dataMarker instanceof AnchorMarker) { //MeasureMarker, AnchorMarker
                        if (firstTime) { //重新create, firstTime為true
                            firstTime = false;
                            this.updateAnchorMarker();
                            StepByStepUndo.add(new DataStepByStep(dataMarker, Selectable.CRUD.CREATE));
                        }

                        Log.d(TAG, "REDO: updateAnchorMarker()");

                    } else if (dataMarker instanceof MeasureMarker) {
                        this.updateMeasureMarker(data);
                        Log.d(TAG, "REDO: updateMeasureMarker()");
                        StepByStepUndo.add(new DataStepByStep(dataMarker, Selectable.CRUD.CREATE));

                    }
                    break;
            }
        }

    }

    public void deselectTool() {
        this.tool = null;
    }

    public Toolbox.Tool getTool() {
        return tool;
    }

    public void holdMarker(Marker marker) { //The Marker will be hold after long pressing
        Muninn.sound_Ding.seekTo(0); //重至0毫秒
        Muninn.sound_Ding.start();
        markerHold = marker;
    }

    //after releasing the marker by hand
    public void releaseMarker() {
        if (markerHold != null) {
            Muninn.sound_Ding.seekTo(0);
            Muninn.sound_Ding.start();
            if (markerHold instanceof ControlMarker) {
                //LinkedMarker //include Anchor's and Measure's ControlMarker 更新ControlMarker
                Log.d(TAG, "#### Release ControlMarker ####");
                Marker fatherMarker = ((ControlMarker)markerHold).getLinksFatherMarker();

                Marker markerHold_linkedMarker = markerHold;
                DataStepByStep update = new DataStepByStep(fatherMarker, Selectable.CRUD.UPDATE);
                fatherMarker.historyLayerPositionsUndo.addLast(new Position(fatherMarker.historyLayerPositionsUndo.getLast().x, fatherMarker.historyLayerPositionsUndo.getLast().y)); //把最新位置新增至LinkedList最後
                markerHold_linkedMarker.refreshed_Layer_position.set(new Position(markerHold.refreshed_tap_position.x, markerHold.refreshed_tap_position.y));
                markerHold_linkedMarker.historyLayerPositionsUndo.addLast(new Position(markerHold_linkedMarker.refreshed_Layer_position.x, markerHold_linkedMarker.refreshed_Layer_position.y)); //copy original

                if (fatherMarker instanceof AnchorMarker) {
                    AnchorMarker.historyDistancesUndo.addLast(AnchorMarker.historyDistancesUndo.getLast());
                }
                StepByStepUndo.add(update);//update the last location of marker
                Log.d(TAG, "StepByStepUndo Size: " + StepByStepUndo.size());

                Log.d(TAG, "historySize = (Marker, Linker) = (" + fatherMarker.historyLayerPositionsUndo.size() + ", " + markerHold_linkedMarker.historyLayerPositionsUndo.size() + ")");
                Log.d(TAG, "Marker Position = " + fatherMarker.refreshed_Layer_position.x + ", " + fatherMarker.refreshed_Layer_position.y);
                Log.d(TAG, "Linker Position = " + markerHold_linkedMarker.refreshed_Layer_position.x + ", " + markerHold_linkedMarker.refreshed_Layer_position.y);
                Log.d(TAG, "Undo List: Marker Position = " + fatherMarker.historyLayerPositionsUndo.getLast().x + ", " + fatherMarker.historyLayerPositionsUndo.getLast().y);
                Log.d(TAG, "Undo List: Linker Position = " + markerHold_linkedMarker.historyLayerPositionsUndo.getLast().x + ", " + markerHold_linkedMarker.historyLayerPositionsUndo.getLast().y);Log.d(TAG, "-----------------------------------------------------------------------------");

            } else if (markerHold instanceof MeasureMarker) {
                //MeasureMarker 更新MeasureMarker
                Log.d(TAG, "#### Release MeasureMarker ####");
                Marker markerHold_linkedMarker = ((MeasureMarker) markerHold).getLink();
                DataStepByStep update = new DataStepByStep(markerHold, Selectable.CRUD.UPDATE);

                markerHold.refreshed_Layer_position.set(new Position(markerHold.refreshed_tap_position.x, markerHold.refreshed_tap_position.y));
                markerHold.historyLayerPositionsUndo.addLast(new Position(markerHold.refreshed_Layer_position.x, markerHold.refreshed_Layer_position.y)); //把最新位置新增至LinkedList最後
                markerHold_linkedMarker.historyLayerPositionsUndo.addLast(new Position(markerHold_linkedMarker.historyLayerPositionsUndo.getLast().x, markerHold_linkedMarker.historyLayerPositionsUndo.getLast().y)); //copy original

                Log.d(TAG, "historySize = (Marker, Linker) = (" + markerHold.historyLayerPositionsUndo.size() + ", " + markerHold_linkedMarker.historyLayerPositionsUndo.size() + ")");
                Log.d(TAG, "Marker Position = " + markerHold.refreshed_Layer_position.x + ", " + markerHold.refreshed_Layer_position.y);
                Log.d(TAG, "Linker Position = " + markerHold_linkedMarker.refreshed_Layer_position.x + ", " + markerHold_linkedMarker.refreshed_Layer_position.y);
                Log.d(TAG, "Undo List: Marker Position = " + markerHold.historyLayerPositionsUndo.getLast().x + ", " + markerHold.historyLayerPositionsUndo.getLast().y);
                Log.d(TAG, "Undo List: Linker Position = " + markerHold_linkedMarker.historyLayerPositionsUndo.getLast().x + ", " + markerHold_linkedMarker.historyLayerPositionsUndo.getLast().y);
                Log.d(TAG, "-----------------------------------------------------------------------------");
                StepByStepUndo.add(update);//update the last location of marker
                Log.d(TAG, "StepByStepUndo Size: " + StepByStepUndo.size());

            } else if (markerHold instanceof AnchorMarker) {
                //AnchorMarker
                Log.d(TAG, "#### Release AnchorMarker ####");
                final Marker marker = AnchorMarker.getInstance();
                Marker markerHold_linkedMarker = AnchorMarker.getInstance().getLink(); //this link was created by marker.

                DataStepByStep update = new DataStepByStep(marker, Selectable.CRUD.UPDATE);

                marker.refreshed_Layer_position.set(new Position(markerHold.refreshed_tap_position.x, markerHold.refreshed_tap_position.y));
                marker.historyLayerPositionsUndo.addLast(new Position(marker.refreshed_Layer_position.x, marker.refreshed_Layer_position.y)); //把最新位置新增至LinkedList最後
                markerHold_linkedMarker.historyLayerPositionsUndo.addLast(new Position(markerHold_linkedMarker.historyLayerPositionsUndo.getLast().x, markerHold_linkedMarker.historyLayerPositionsUndo.getLast().y)); //copy original

                AnchorMarker.historyDistancesUndo.addLast(AnchorMarker.historyDistancesUndo.getLast());

                Log.d(TAG, "historySize = (Marker, Linker, Distance) = (" + markerHold.historyLayerPositionsUndo.size() + ", " + markerHold_linkedMarker.historyLayerPositionsUndo.size() + ", " + AnchorMarker.historyDistancesUndo.size() + ")");
                Log.d(TAG, "Marker Position = " + markerHold.refreshed_Layer_position.x + ", " + markerHold.refreshed_Layer_position.y);
                Log.d(TAG, "Linker Position = " + markerHold_linkedMarker.refreshed_Layer_position.x + ", " + markerHold_linkedMarker.refreshed_Layer_position.y);
                Log.d(TAG, "Undo List: Marker Position = " + markerHold.historyLayerPositionsUndo.getLast().x + ", " + markerHold.historyLayerPositionsUndo.getLast().y);
                Log.d(TAG, "Undo List: Linker Position = " + markerHold_linkedMarker.historyLayerPositionsUndo.getLast().x + ", " + markerHold_linkedMarker.historyLayerPositionsUndo.getLast().y);
                Log.d(TAG, "The latest distance = " + AnchorMarker.historyDistancesUndo.getLast());
                Log.d(TAG, "-----------------------------------------------------------------------------");
                StepByStepUndo.add(update);//update the last location of marker
                Log.d(TAG, "StepByStepUndo Size: " + StepByStepUndo.size());


            } else if (markerHold instanceof LabelMarker) {
                //LabelMarker
                Log.d(TAG, "#### Release LabelMarker ####");

                DataStepByStep update = new DataStepByStep(markerHold, Selectable.CRUD.UPDATE);
                markerHold.refreshed_Layer_position.set(new Position(markerHold.refreshed_tap_position.x, markerHold.refreshed_tap_position.y));
                markerHold.historyLayerPositionsUndo.addLast(new Position(markerHold.refreshed_Layer_position.x, markerHold.refreshed_Layer_position.y)); //把最新位置新增至LinkedList最後
                Log.d(TAG, "historyLayerPositionsUndo Size = " + markerHold.historyLayerPositionsUndo.size());
                Log.d(TAG, "new Position = " + markerHold.refreshed_Layer_position.x + "," + markerHold.refreshed_Layer_position.y);
                StepByStepUndo.add(update);//update the last location of marker
                Log.d(TAG, "StepByStepUndo Size: " + StepByStepUndo.size());
            }



            markerHold = null;
        }

    }

    void dealMeasureMarker(Marker marker, Marker link) {

    }

    public void selectMarker() {
        this.markerSelected = this.markerSelecting; //?Jonas
        this.markerSelecting = null;
        if (this.markerSelected != null)
            this.markerSelected.select(); //change state
    }

    public void deselectMarker() {
        if (this.markerSelected != null)
            this.markerSelected.deselect();
        if (this.markerSelecting != null)
            this.markerSelecting.deselect();
        this.markerSelecting = null;
        this.markerSelected = null;
    }

    public void selectingMarker(Marker marker) {
        this.markerSelecting = marker;
        if (this.markerSelecting != null)
            this.markerSelecting.selecting();
    }

    public void setMarkerType(Type type) {
        if (type.toString().contains("Marker"))
            this.markerType = type;
    }

    public void moveHoldMarker(Position position) {
        if (this.markerHold != null) {
            Log.d(TAG, "#### Moving ControlMarker ####");

            draft.moveMarker(markerHold, position);
//            markerHold.refreshed_tap_position = position; //儲存marker移動的位置(螢幕點選的位置)//?Jonas
        }
    }

    double getAngle(Marker A, Marker B) {
        double angle = 0.0;
        double X = A.position.x - B.position.x;
        double Y = A.position.y - B.position.y;
        angle = Math.atan(Y/X);

        return angle;
    }


    public void zoomDraft(float scale_offset) {
        this.draft.layer.scale(scale_offset);
    }

    public void moveDraft(Position offset) {
        this.draft.layer.moveLayer(offset);
    }

    private void showToast(String string, boolean is_short) {
        Toast.makeText(Muninn.getContext(), string, is_short ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
    }

    private File makeSignDirectory(String MD5) {
        File directory = new File(Environment.getExternalStorageDirectory(), MD5);
        if(!directory.exists()) {
            directory.mkdir();
        }
        return directory;
    }

    public void showSignPad(Context context) {
        final SignPad signpad = new SignPad(Muninn.getContext());
        try {
            String MD5 = "";
            if(MD5EncoderThread != null && MD5Encoder != null) {
                MD5EncoderThread.join();
                MD5 = MD5Encoder.getResult();
                final File directory = makeSignDirectory(MD5);
                new AlertDialog.Builder(context)
                        .setTitle("簽名 請註記" + MD5)
                        .setView(signpad)
                        .setPositiveButton("儲存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final Bitmap bitmap = signpad.exportBitmapRenderedOnCanvas();
                                Date date = new Date();
                                SimpleDateFormat date_format = new SimpleDateFormat("yyyyMMddHHmmss");
                                String filename = date_format.format(date);
                                try {
                                    File file = new File(directory, filename);
                                    FileOutputStream output_stream = new FileOutputStream(file);
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, output_stream);
                                    output_stream.flush();
                                    output_stream.close();
                                    showToast("儲存成功", true);
                                    signFiles.add(file);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .show();
            }
            else {
                showToast("請開啟影像", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File exportToDOM() {
        File file = new File(Environment.getExternalStorageDirectory(), "data.xml");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Node node = draft.writeDOM(document);
            document.appendChild(node);
            if(MD5EncoderThread != null && MD5Encoder != null) {
                Node root = document.getElementsByTagName("Draft").item(0);
                Node md5_code_tag = document.createElement("code");
                MD5EncoderThread.join();
                md5_code_tag.setTextContent(MD5Encoder.getResult());
                root.appendChild(md5_code_tag);
            }
            TransformerFactory transformer_factory = TransformerFactory.newInstance();
            Transformer transformer = transformer_factory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    private void WriteDOMFileToZIP(File DOM_file, ZipOutputStream zip_stream, int BUFFER) {
        try {
            byte data[] = new byte[BUFFER];
            FileInputStream file_input = new FileInputStream(DOM_file);
            BufferedInputStream origin = new BufferedInputStream(file_input, BUFFER);
            ZipEntry entry = new ZipEntry(DOM_file.getName());
            zip_stream.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                zip_stream.write(data, 0, count);
            }
            origin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void WriteBitmapToZIP(String filename, Bitmap bitmap, ZipOutputStream zip_stream, int BUFFER) {
        byte data[] = new byte[BUFFER];
        if (bitmap != null) {
            try {
                File image_file = new File(Environment.getExternalStorageDirectory(), filename + ".png");
                FileOutputStream bitmap_file = new FileOutputStream(image_file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bitmap_file);
                FileInputStream file_input = new FileInputStream(image_file);
                BufferedInputStream origin = new BufferedInputStream(file_input, BUFFER);
                ZipEntry entry = new ZipEntry(filename + ".png");
                zip_stream.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    zip_stream.write(data, 0, count);
                }
                origin.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void exportToZip() {
        File data_file = exportToDOM();
        Muninn.soundPlayer.start();
        showToast("開始儲存，靜候完成訊息。", true);
        if (data_file.exists()) {
            try {
                Date current_time = new Date();
                SimpleDateFormat simple_date_format = new SimpleDateFormat("Draft yyyyMMddHHmmss");
                String filename = simple_date_format.format(current_time) + ".zip";
                FileOutputStream destination = new FileOutputStream(new File(Environment.getExternalStorageDirectory(), filename));
                ZipOutputStream zip_stream = new ZipOutputStream(new BufferedOutputStream(destination));
                final int BUFFER = 256;
                WriteDOMFileToZIP(data_file, zip_stream, BUFFER);

                final Bitmap bitmap = draftRenderer.getBirdview();
                WriteBitmapToZIP("birdview", bitmap, zip_stream, BUFFER);

                for(File file : signFiles) {
                    Bitmap sign_bitmap = BitmapFactory.decodeFile(file.getPath());
                    WriteBitmapToZIP(file.getName(), sign_bitmap, zip_stream, BUFFER);
                }

                zip_stream.close();
                destination.close();
                Muninn.soundPlayer.start();
                showToast("儲存成功", true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }
        }
    }

    public void uploadToSever(Uri uri) {
        try {
            Muninn.soundPlayer.start();
            showToast("開始上傳，靜候完成訊息。", true);
            InputStream stream = context.getContentResolver().openInputStream(uri);
            final SharedPreferences shared_preferences = Muninn.getSharedPreferences();
            String server_address = shared_preferences.getString("server_address", "134.208.2.201");
            String username = shared_preferences.getString("username", "demo");
            String password = shared_preferences.getString("password", "demo");
            String folder = shared_preferences.getString("server_folder", "/");
            FTPUploader uploader = new FTPUploader(server_address, username, password, 21);
            uploader.folder = folder;
            Date date = new Date();
            SimpleDateFormat date_format = new SimpleDateFormat("yyyyMMddHHmmss");
            String filename = date_format.format(date);
            uploader.setFile(filename + ".zip", stream);
            Thread thread = new Thread(uploader);
            thread.start();
            thread.join();
            Muninn.soundPlayer.start();
            showToast(uploader.error ? "上傳失敗" : "已上傳。", true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
