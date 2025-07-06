"use client";
import {Controller, SubmitHandler, useForm} from "react-hook-form";
import {refreshArticles} from "../actions";
import MDEditor from "@uiw/react-md-editor";
import React, {useEffect} from "react";
import {useToast} from "@/components/hooks/use-toast";
import {useTimeline} from "@/app/hooks/useTimelines";

type Inputs = {
    name: string;
    description: string;
};

export default function TimelineForm({id}: {id?: number}) {
    const {
        register,
        handleSubmit,
        watch,
        setValue,
        reset,
        formState: {errors},
        control,
    } = useForm<Inputs>({
        defaultValues: {
            name: "Example Timeline",
            description: "Example Description",
        },
    });

    const {toast} = useToast();
    const {timeline, mutate: mutateTimeline} = useTimeline(id?.toString() || "");
    const onSubmit: SubmitHandler<Inputs> = async (data) => {
        if(id){
            await handleUpdateTimeline(id, data);
        }else{
            await handleCreateTimeline(data)
        }
    };

    async function handleCreateTimeline(data:Inputs) {
        const response = await fetch("http://localhost:7070/api/timelines", {
            method: "POST",
            credentials: "include",
            body: JSON.stringify(data),
        });
        if (response.ok) {
            toast({
                title: "Timeline Successfully Created",
                description: data.name,
            });
            await refreshArticles();
            reset();
        }
    }

    async function handleUpdateTimeline(id: number, data: Inputs) {
        const dataWithId = {...data, id}
        const response = await fetch(`http://localhost:7070/api/timelines/${id}`, {
            method: "PUT",
            credentials: "include",
            body: JSON.stringify(dataWithId),
        });
        if (response.ok) {
            toast({
                title: "Timeline Successfully Updated",
                description: data.name,
            });
            await mutateTimeline();
        }
    }

    useEffect(() => {
        if (id && timeline) {
            const {name, description} = timeline;
            setValue("name", name)
            setValue("description", description);
        }
    })

    return (
        <form onSubmit={handleSubmit(onSubmit)}>
            <div className="flex flex-col gap-4 py-4">
                <label className="flex flex-col" htmlFor="name">
                    Name
                    <input
                        {...register("name", {required: true})}
                        id="name"
                        defaultValue="Untitled"
                    />
                </label>
                <label className="flex flex-col" htmlFor="description">
                    Description
                    <Controller
                        name="description"
                        control={control}
                        defaultValue="Empty description"
                        render={({field}) => (
                            <div data-color-mode="light">
                                <MDEditor
                                    value={field.value}
                                    onChange={field.onChange}
                                />
                            </div>
                        )}
                    />
                </label>
            </div>
            <button className="button" type="submit">
                {id ? "SAVE CHANGES" : "SUBMIT"}
            </button>
        </form>
    );
}
