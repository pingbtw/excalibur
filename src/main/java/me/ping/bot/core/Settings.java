package me.ping.bot.core;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Settings {
    private static Settings settings = null;

    private final String path;
    private ArrayList<Long> roleIds;
    private String embedColor = "#FF0000", cmdPrefix = "x!";

    public static Settings getInstance() {
        if (settings == null)
            settings = new Settings();
        return settings;
    }

    private Settings() {
        path = System.getProperty("user.dir") + System.getProperty("file.separator") + "excalibur_settings.conf";
        roleIds = new ArrayList<Long>();
        roleIds.add(1000L);
        roleIds.add(2000L);
    }

    public void loadSettings() {
        try {
            String json = new String(Files.readAllBytes(Paths.get(path)));
            JSONObject obj = new JSONObject(json);
            embedColor = obj.getString("embed_color");
            cmdPrefix = obj.getString("cmd_prefix");
            JSONArray roles = obj.getJSONArray("admin_allowed_roles");

            this.roleIds.removeAll(this.roleIds);
            for (int i = 0; i < roles.length(); i++) {
                this.roleIds.add(Long.parseLong(roles.get(i).toString()));
            }
        } catch (IOException ex) {
            System.out.println("Unable to locate settings.conf");
            System.out.println("Creating " + System.getProperty("user.dir") + System.getProperty("file.separator") + "settings.conf now");
            createSettingsFile(path);
        } catch (JSONException ex) {
            Logger.log("JSON value not found. Please delete and recreate " + path);
            ex.printStackTrace();
            System.exit(1);
        } catch (Exception ex) {
            Logger.log(ex.getMessage());
            System.exit(1);
        }
    }

    private void createSettingsFile(String path) {

        File file = new File(path);
        try {
            if (!file.createNewFile()) {
                System.out.println("Unable to create " + path);
                System.out.println("Please manually create " + path);
                System.exit(1);
            } else {
                updateSettings();
            }
        } catch (IOException ex) {
            System.out.println("Unable to create " + path);
            System.out.println("Please manually create " + path);
            System.exit(1);
        }
    }

    private void updateSettings() {
        try {
            JSONArray roles = new JSONArray();
            for (int i = 0; i < this.roleIds.size(); i++) {
                roles.put(this.roleIds.get(i));
            }

            JSONObject settings = new JSONObject();
            settings.put("cmd_prefix", cmdPrefix);
            settings.put("embed_color", embedColor);
            settings.put("admin_allowed_roles", roles);
            FileWriter writer = new FileWriter(path);
            writer.write(settings.toString(4));
            writer.close();
        } catch (IOException ex) {
            Logger.log("Unable to write to settings.conf");
        } catch (Exception ex) {
            Logger.log(ex.getMessage());
            System.exit(1);
        }
    }


    public String getEmbedColor() {
        return embedColor;
    }

    public void setEmbedColor(String embedColor) {
        this.embedColor = embedColor;
        updateSettings();
    }

    public String getCmdPrefix() {
        return this.cmdPrefix.toLowerCase();
    }

    public void setCmdPrefix(String prefix) {
        this.cmdPrefix = prefix;
        updateSettings();
    }

    public boolean addRole(long roleId) {
        if (!this.roleIds.contains(roleId)) {
            this.roleIds.add(roleId);
            updateSettings();
            return true;
        }
        return false;
    }

    public boolean removeRole(long roleId) {
        if (this.roleIds.contains(roleId)) {
            this.roleIds.remove(roleId);
            updateSettings();
            return true;
        }
        return false;
    }

    public ArrayList<Long> getRoleIds() {
        return this.roleIds;
    }

    public String getRoleIdsAsString() {
        return roleIds.toString();
    }
}
