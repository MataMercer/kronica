import TimelineForm from "@/app/forms/TimelineForm";

export default async function EditTimelinePage(props: {
    params: Promise<{ id: string; page: string }>;
}) {
    const params = await props.params;
    const id = params.id;
    return (
        <div className="self-center">
            <h1 className="text-2xl self-center">Edit Timeline</h1>
            <TimelineForm id={Number(id)} />
        </div>
    );
}
