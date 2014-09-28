package li.barter.chat.backup;

import android.database.Cursor;
import android.util.JsonWriter;

import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import li.barter.data.DatabaseColumns;

/**
 * Class that takes care of Serializing/Deserializing the chat messages
 * <p/>
 * Created by vinaysshenoy on 28/09/14.
 */
public class ChatSerializer {

    private static final String TAG = "ChatSerializer";

    /**
     * The current version of the serialization/deserialization document
     */
    private static final int CURRENT_VERSION = 1;

    /**
     * Begin writing the chats data out
     *
     * @param chatsCursor    The cursor pointing to the Chats table
     * @param messagesCursor The cursor pointing to the Chat messages table
     * @param out            The output stream to write the chats to
     * @throws java.io.UnsupportedEncodingException, java.io.IOException
     */
    public void beginWrite(final Cursor chatsCursor, final Cursor messagesCursor, final OutputStream out) throws IOException {

        final JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, HTTP.UTF_8));
        writer.beginObject();

        writeHeader(writer);
        writeChatsOut(chatsCursor, writer);
        writeChatMessagesOut(messagesCursor, writer);

        writer.endObject();
        writer.close();
    }

    /**
     * Writes the header to the output stream
     *
     * @param writer The Json writer that is used to write out the chats
     * @throws java.io.IOException
     */
    private void writeHeader(final JsonWriter writer) throws IOException {

        writer.name("version").value(CURRENT_VERSION);
    }

    /**
     * Write the individual chats out to a stream
     *
     * @param cursor The cursor pointing to the Chats table
     * @param writer The Json writer that is used to write out the chats
     * @throws java.io.IOException
     */
    private void writeChatsOut(final Cursor cursor, final JsonWriter writer) throws IOException {

        writer.name("chats");
        writer.beginArray();

        while (cursor.moveToNext()) {
            writeSingleChatOut(cursor, writer);
        }
        writer.endArray();
    }

    /**
     * Write individual chats out to the stream
     *
     * @param cursor The cursor forwarded to the chat to write
     * @param writer The Json writer that is used to write out the chats
     * @throws java.io.IOException
     */
    private void writeSingleChatOut(final Cursor cursor, final JsonWriter writer) throws IOException {

        writer.beginObject();
        writer.name("chat_id").value(cursor.getString(cursor.getColumnIndex(DatabaseColumns.CHAT_ID)));
        writer.name("chat_type").value(cursor.getString(cursor.getColumnIndex(DatabaseColumns.CHAT_TYPE)));
        writer.name("last_message_id").value(cursor.getString(cursor.getColumnIndex(DatabaseColumns.LAST_MESSAGE_ID)));
        writer.name("user_id").value(cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_ID)));
        writer.name("unread_count").value(cursor.getLong(cursor.getColumnIndex(DatabaseColumns.UNREAD_COUNT)));
        writer.name("timestamp").value(cursor.getString(cursor.getColumnIndex(DatabaseColumns.TIMESTAMP)));
        writer.name("timestamp_human").value(cursor.getString(cursor.getColumnIndex(DatabaseColumns.TIMESTAMP_HUMAN)));
        writer.name("timestamp_epoch").value(cursor.getLong(cursor.getColumnIndex(DatabaseColumns.TIMESTAMP_EPOCH)));
        writer.endObject();
    }

    /**
     * Write the individual chat messages out to a stream
     *
     * @param cursor The cursor pointing to the Chat messages table
     * @param writer The Json writer that is used to write out the chats
     * @throws java.io.IOException
     */
    private void writeChatMessagesOut(final Cursor cursor, final JsonWriter writer) throws IOException {

        writer.name("messages");
        writer.beginArray();

        while (cursor.moveToNext()) {
            writeSingleMessageOut(cursor, writer);
        }

        writer.endArray();
    }

    /**
     * Write individual messages out to the stream
     *
     * @param cursor The cursor forwarded to the message to write
     * @param writer The Json writer that is used to write out the chats
     * @throws java.io.IOException
     */
    private void writeSingleMessageOut(final Cursor cursor, final JsonWriter writer) throws IOException {

        writer.beginObject();
        writer.name("chat_id").value(cursor.getString(cursor.getColumnIndex(DatabaseColumns.CHAT_ID)));
        writer.name("sender_id").value(cursor.getString(cursor.getColumnIndex(DatabaseColumns.SENDER_ID)));
        writer.name("receiver_id").value(cursor.getString(cursor.getColumnIndex(DatabaseColumns.RECEIVER_ID)));
        writer.name("user_id").value(cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_ID)));
        writer.name("sent_at").value(cursor.getString(cursor.getColumnIndex(DatabaseColumns.SENT_AT)));
        writer.name("message").value(cursor.getString(cursor.getColumnIndex(DatabaseColumns.MESSAGE)));
        writer.name("timestamp").value(cursor.getString(cursor.getColumnIndex(DatabaseColumns.TIMESTAMP)));
        writer.name("timestamp_human").value(cursor.getString(cursor.getColumnIndex(DatabaseColumns.TIMESTAMP_HUMAN)));
        writer.name("timestamp_epoch").value(cursor.getLong(cursor.getColumnIndex(DatabaseColumns.TIMESTAMP_EPOCH)));
        writer.name("chat_status").value(cursor.getLong(cursor.getColumnIndex(DatabaseColumns.CHAT_STATUS)));
        writer.endObject();
    }
}
