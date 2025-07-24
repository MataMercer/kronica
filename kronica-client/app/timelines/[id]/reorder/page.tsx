import ReorderTimelineForm from "@/app/forms/ReorderTimelineForm";

export default async function ReorderTimelinePage(props: {
    params: Promise<{ id: string; page: string }>;
}) {
    const params = await props.params;
    const id = params.id;
    return (
        <div className="self-center">
            <h1 className="text-2xl self-center">Edit Timeline</h1>
            <ReorderTimelineForm timelineId={Number(id)} />
        </div>
    );
}
