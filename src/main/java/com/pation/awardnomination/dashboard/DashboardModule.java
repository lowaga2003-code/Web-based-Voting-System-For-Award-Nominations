package com.pation.awardnomination.dashboard;

import java.util.List;

/**
 * [INTEGRATION] One card on the main dashboard = one member's module.
 *
 * (සිංහලෙන්: Dashboard එකේ පේන එක module card එකක්. links හිස් නම් ඒ member ගේ part එක
 *  තාම system එකට merge කරලා නැහැ -> "Coming soon" විදියට පේනවා.)
 *
 * When another member's part is merged, give its card one or more links in
 * DashboardController#buildModules and it becomes clickable.
 */
public class DashboardModule {

    /** A button on the card, e.g. "Manage users" -> /admin/users */
    public static class Link {
        private final String label;
        private final String url;

        public Link(String label, String url) {
            this.label = label;
            this.url = url;
        }

        public String getLabel() { return label; }
        public String getUrl() { return url; }
    }

    private final String key;          // short id used by dashboard.jsp for the icon + colour, e.g. "nominee"
    private final String code;         // e.g. "Module 3"
    private final String title;        // e.g. "Nominee Profile & Evidence"
    private final String actor;        // who uses it, e.g. "Nominee"
    private final String description;
    private final String ownerRole;    // main-login role this module belongs to, e.g. ROLE_NOMINEE
    private final List<Link> links;    // empty list = not merged yet
    private final boolean forEveryone; // true = every logged-in user sees this card (e.g. raising a dispute)

    public DashboardModule(String key, String code, String title, String actor, String description,
                           String ownerRole, List<Link> links) {
        this(key, code, title, actor, description, ownerRole, links, false);
    }

    public DashboardModule(String key, String code, String title, String actor, String description,
                           String ownerRole, List<Link> links, boolean forEveryone) {
        this.key = key;
        this.code = code;
        this.title = title;
        this.actor = actor;
        this.description = description;
        this.ownerRole = ownerRole;
        this.links = links;
        this.forEveryone = forEveryone;
    }

    public String getKey() { return key; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public String getActor() { return actor; }
    public String getDescription() { return description; }
    public String getOwnerRole() { return ownerRole; }
    public List<Link> getLinks() { return links; }
    public boolean isForEveryone() { return forEveryone; }

    /** true when the module is merged into the system and has at least one page to open. */
    public boolean isConnected() { return links != null && !links.isEmpty(); }
}
