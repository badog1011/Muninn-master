package studio.bachelor.draft;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
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
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import studio.bachelor.draft.marker.AnchorMarker;
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
import studio.bachelor.draft.utility.MapString;
import studio.bachelor.draft.utility.Position;
import studio.bachelor.draft.utility.Renderable;
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
    public static final DraftDirector instance = new DraftDirector();
    private Draft draft;
    private DraftRenderer draftRenderer;
    private RendererManager rendererManager;
    private Map<Object, Renderable> renderableMap = new HashMap<Object, Renderable>();
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
            MD5Encoder = new BitmapMD5Encoder(birdview);
            MD5EncoderThread = new Thread(MD5Encoder);
            MD5EncoderThread.start();
        } catch (Exception e) {
            Log.d("DraftRenderer", "setBirdview(Uri uri)" + e.toString());
        }
        draftRenderer.setBirdview(birdview);
        draft.setWidth(birdview.getWidth());
        draft.setHeight(birdview.getHeight());
    }

    public void setWidthAndHeight(float width, float height) {
        this.draft.setWidthAndHeight(width, height);
    }

    public void setToolboxRenderer(Position upper_left_corner, float width, float height) {
        toolboxRenderer = new ToolboxRenderer(toolbox, upper_left_corner, width, height);
    }

    public void createPathIfPathMode(Position position) {
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

    public void addMarker(Position position) {
        if (markerType == MeasureMarker.class) {
            addMeasureMarker(position);
        } else if (markerType == AnchorMarker.class) {
            addAnchorMarker(position);
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
                .show();

        draft.addMarker(marker);

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
    }

    private void addAnchorMarker(Position position) {
        //  取得AnchorMarker與ControlMaker
        final Marker marker = AnchorMarker.getInstance();
        Marker linked = AnchorMarker.getInstance().getLink();

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
                        if (distance_str.isEmpty())
                            return;
                        ((AnchorMarker) marker).setRealDistance(Double.parseDouble(distance_str));
                    }
                })
                .show();

        marker.position.set(position);
        linked.position.set(new Position(position.x + 50, position.y + 50));

        draft.addMarker(marker);
        draft.addMarker(linked);

        Position[] positions = {marker.position, linked.position};
        List<Position> position_list = new ArrayList<Position>(Arrays.asList(positions));

        //  建立MakerRenderer
        MarkerRendererBuilder mrb = new MarkerRendererBuilder();
        Renderable marker_renderer = mrb.
                setLinkLine((LinkMarker) marker).
                setReference(marker).
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
        if (marker == null)
            return;
        if (renderableMap.containsKey(marker)) {
            Renderable renderable = renderableMap.get(marker);
            rendererManager.removeRenderer(renderable);
            renderableMap.remove(marker);
        }
        draft.removeMarker(marker);
    }

    private void addMeasureMarker(Position position) {
        //  建立LinkMaker與ControlMaker
        ControlMarkerBuilder cb = new ControlMarkerBuilder();
        Marker linked = cb.
                setPosition(new Position(position.x - 100, position.y)).
                build();
        LinkMarkerBuilder lb = new MeasureMarkerBuilder();
        Marker marker = lb.
                setPosition(position).
                setLink(linked).
                build();

        draft.addMarker(marker);
        draft.addMarker(linked);

        Position[] positions = {marker.position, linked.position};
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
                setReference(linked).
                build();

        rendererManager.addRenderer(marker_renderer);
        rendererManager.addRenderer(link_renderer);

        //  建立對應關係
        renderableMap.put(marker, marker_renderer);
        renderableMap.put(linked, link_renderer);
    }

    public Marker getNearestMarker(Position position) {
        return draft.getNearestMarker(position);
    }

    public Toolbox.Tool getNearestTool(Position position) {
        return toolboxRenderer.getInstance(position, 64);
    }

    public void render(Canvas canvas) {
        canvas.save();
        draftRenderer.onDraw(canvas);

        for (Renderable renderable : rendererManager.renderObjects) {
            renderable.onDraw(canvas);
        }

        canvas.restore();

        if (toolboxRenderer != null)
            toolboxRenderer.onDraw(canvas);

        if (tool != null) {
            Bitmap bitmap = ToolboxRenderer.getToolIcon(tool);
            canvas.drawBitmap(bitmap, canvas.getWidth() - bitmap.getWidth(), canvas.getHeight() - bitmap.getHeight(), paint);
        }
    }

    public void selectTool(Toolbox.Tool tool) {
        if (tool == Toolbox.Tool.CLEAR_PATH)
            draft.clearPaths();
        else
            this.tool = tool;
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
        }
    }

    public void deselectTool() {
        this.tool = null;
    }

    public Toolbox.Tool getTool() {
        return tool;
    }

    public void holdMarker(Marker marker) {
        markerHold = marker;
    }

    public void releaseMarker() {
        markerHold = null;
    }

    public void selectMarker() {
        this.markerSelected = this.markerSelecting;
        this.markerSelecting = null;
        if (this.markerSelected != null)
            this.markerSelected.select();
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
            draft.moveMarker(markerHold, position);
        }
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
                SimpleDateFormat simple_date_format = new SimpleDateFormat("yyyyMMddHHmmss");
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
