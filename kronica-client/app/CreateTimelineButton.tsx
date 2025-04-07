"use client";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from "@/components/ui/dialog";
import { Controller, SubmitHandler, useForm } from "react-hook-form";
import { refreshArticles } from "./actions";
import MDEditor from "@uiw/react-md-editor";
import { useState } from "react";
import React from "react";
import UploadInput, { FileInput } from "./components/inputs/UploadInput";

type Inputs = {
    name: string;
    description: string;
};

export default function CreateTimeline() {
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

    const [showTimelineForm, setShowTimelineForm] = useState(false);
    const ref = React.useRef();
    const onSubmit: SubmitHandler<Inputs> = async (data) => {
        const response = await fetch("http://localhost:7070/api/timelines", {
            method: "POST",
            credentials: "include",
            body: JSON.stringify(data),
        });

        refreshArticles();
        // setShowArticleForm(false);
        ref.current?.click();
        reset();
    };

    return (
        <Dialog>
            <DialogTrigger asChild ref={ref}>
                <button
                    className="button"
                    onClick={() => {
                        setShowTimelineForm(true);
                    }}
                >
                    CREATE TIMELINE
                </button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-[90vw]">
                <form onSubmit={handleSubmit(onSubmit)}>
                    <DialogHeader>
                        <DialogTitle>CREATE TIMELINE</DialogTitle>
                    </DialogHeader>
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
                    <DialogFooter>
                        <button className="button" type="submit">
                            Submit
                        </button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
}
