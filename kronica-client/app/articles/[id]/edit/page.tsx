import ArticleForm from "@/app/forms/ArticleForm";

export default async function CreateArticlePage(props: {
    params: Promise<{ id: string; page: string }>;
}) {
    const params = await props.params;
    const id = params.id;
    return (
        <div className="self-center">
            <h1 className="text-2xl self-center">Edit Article</h1>
            <ArticleForm id={Number(id)} />
        </div>
    );
}
