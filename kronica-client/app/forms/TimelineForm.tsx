"use client";
import { Controller, SubmitHandler, useForm } from "react-hook-form";
import { refreshArticles } from "../actions";
import MDEditor from "@uiw/react-md-editor";
import React from "react";
import { useToast } from "@/components/hooks/use-toast";

type Inputs = {
    name: string;
    description: string;
};

export default function TimelineForm() {
    const {
        register,
        handleSubmit,
        watch,
        setValue,
        reset,
        formState: { errors },
        control,
    } = useForm<Inputs>({
        defaultValues: {
            name: "Example Timeline",
            description: "Example Description",
        },
    });

    const { toast } = useToast();

    const ref = React.useRef(undefined);
    const onSubmit: SubmitHandler<Inputs> = async (data) => {
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

            refreshArticles();
            reset();
        }
    };

    return (
        <form onSubmit={handleSubmit(onSubmit)}>
            <div className="flex flex-col gap-4 py-4">
                <label className="flex flex-col" htmlFor="name">
                    Name
                    <input
                        {...register("name", { required: true })}
                        id="name"
                        defaultValue="Untitled"
                    />
                </label>
                <label className="flex flex-col" htmlFor="description">
                    Description
                    {/* <textarea
                                    {...register("body", { required: true })}
                                    id="body"
                                    defaultValue="Empty body"
                                    rows={20}
                                /> */}
                    <Controller
                        name="description"
                        control={control}
                        defaultValue="Empty description"
                        render={({ field }) => (
                            <div data-color-mode="light">
                                <MDEditor
                                    value={field.value}
                                    onChange={field.onChange}
                                />
                                {/* <MDEditor.Markdown
                                                source={field.value}
                                                style={{
                                                    whiteSpace: "pre-wrap",
                                                }}
                                            /> */}
                            </div>
                        )}
                    />
                </label>
            </div>
            <button className="button" type="submit">
                Submit
            </button>
        </form>
    );
}
