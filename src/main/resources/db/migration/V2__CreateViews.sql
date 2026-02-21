CREATE VIEW authors_view AS
    SELECT
        users.id AS authors_id,
        users.name AS authors_name,
        users.role AS authors_role
    FROM users;


CREATE VIEW articles_view AS
    SELECT
        articles.*,

        authors_view.*,

        content.created_at AS created_at,
        content.updated_at AS updated_at,
        content.nsfw AS nsfw,


        timeline_entries.timeline_index AS timeline_entries_timeline_index,

        timelines.id AS timelines_id,
        timelines.name AS timelines_name,
        timelines.description AS timelines_description,

        count(*) OVER() AS total_count
    FROM articles
    INNER JOIN content
        ON articles.id=content.id
    INNER JOIN authors_view
        ON content.author_id=authors_view.authors_id
    LEFT JOIN timeline_entries
        ON articles.id=timeline_entries.article_id
    LEFT JOIN timelines
        ON timeline_entries.timeline_id=timelines.id;